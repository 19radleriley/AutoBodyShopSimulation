package Simulation;
/**
 * @author Riley Radle
 * 
 * Description: 
 *    This class models a process oriented 
 *    perspective of a mechanic entity within 
 *    the Auto Body Shop model. (Permanent entity)
 * 
 * Last Edited: August 2021
 */

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class Mechanic extends SimProcess 
{
   /**
    * @param owner
    * @param name
    * @param showInTrace
    * @param salaryPerDay
    * @param payPerCustomer
    * @param mean
    * @param referRate
    */
   public Mechanic(Model owner, String name, boolean showInTrace, double salaryPerDay, 
         double payPerCustomer, double mean,  double referRate) 
   {
      super(owner, name, showInTrace);
   }

   @Override
   /**
    * Models the life cycle of a Mechanic in the Auto Body Shop:
    *   1. Checks for a waiting customer
    *   2. If there isn't one -> Passivate
    *   3. If there is one ->
    *     a. Attempt to fix their car
    *     b. Potentially refer to specialist if they can't fix it
    *     c. Potentially send them to other body shop 
    */
   public void lifeCycle() throws SuspendExecution 
   {
      // Initializations and stat updates
      AutoBodyShop mc = (AutoBodyShop)getModel();
      mc.todaysCost.update(AutoBodyShop.MECHANIC_SALARY);
      
      while (true)
      {
         // There is not another car.
         if (mc.waitingForMechanic.isEmpty())
         {
            mc.idleMechanics.insert(this);
            this.passivate();
         }
         // There is another car.
         else
         {
            // Get the next car.
            Customer seeingMechanic = mc.waitingForMechanic.removeFirst();
            
            // Sample and hold for time t.
            double time = mc.mechanicFixTimes.sample();
            this.hold(new TimeSpan(time));
            
            // SEEING MECHANIC ...
           
            boolean referred = mc.mechanicReferral.sample();
            
            // The customer needs to be referred to the specialist. 
            if (referred)
            {               
               // Customer has been in system for > 30 minutes -> Will leave to go to other body shop
               if ( (mc.presentTime().getTimeAsDouble() - seeingMechanic.arrivalTime) > 0.5 ) 
               {
                  seeingMechanic.finished = true;
                                    
                  // Update stats 
                  mc.totalLost.update(); // += 1
                  mc.todaysCost.update(AutoBodyShop.LOSS_COST);
               }
               // Else car will be sent to specialist.
               else 
               {
                  mc.waitingForSpecialist.insert(seeingMechanic);
               }
               
            }
            // The car does not need referral.
            else
            {
               seeingMechanic.finished = true;
               
               // Update stats. 
               mc.fullyFixed.update(); // += 1
               mc.responseTimes.update(mc.presentTime().getTimeAsDouble() - seeingMechanic.arrivalTime);
            }
            
            // Reactivate customer.
            seeingMechanic.activate();
            
            // Pay the mechanic' commission.
            mc.todaysCost.update(AutoBodyShop.MECHANIC_COMMISSION);
         }
      }
   }
}