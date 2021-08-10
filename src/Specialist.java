/**
 * @author Riley Radle
 * 
 * Description: 
 *    This class models a process oriented 
 *    perspective of a specialist mechanic within 
 *    the Auto Body Shop model. (Permanent entity)
 * 
 * Last Edited: August 2021
 */

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class Specialist extends SimProcess 
{
   /**
    * @param owner
    * @param name
    * @param showInTrace
    * @param salaryPerDay
    * @param payPerCustomer
    * @param mean
    */
   public Specialist(Model owner, String name, boolean showInTrace, 
         double salaryPerDay, double payPerCustomer, double mean) 
   {
      super(owner, name, showInTrace);
   }

   @Override
   /**
    * Models the life cycle of a specialist in the Shop:
    *   1. Checks for a waiting car
    *   2. If there is one -> fix the car (ie. hold)
    *   3. Else -> wait until there is a car (ie. passivate) 
    */
   public void lifeCycle() throws SuspendExecution 
   {
      // Get the model. 
      AutoBodyShop mc = (AutoBodyShop)getModel();
      mc.todaysCost.update(AutoBodyShop.SPECIALIST_SALARY);
      
      while (true)
      {
         // There is not another car to fix.
         if (mc.waitingForSpecialist.isEmpty())
         {
            mc.idleSpecialists.insert(this);
            this.passivate();
         }
         // There is another car to fix.
         else
         {
            // Get the next customer.
            Customer seeingSpecialist = mc.waitingForSpecialist.removeFirst();
            
            // Sample and hold for time t.
            double time = mc.specialistFixTimes.sample();
            this.hold(new TimeSpan(time));
            
            // SEEING SPECIALIST ...
              
            // Update stats 
            mc.fullyFixed.update(); // += 1
            mc.responseTimes.update(mc.presentTime().getTimeAsDouble() - seeingSpecialist.arrivalTime); 
            seeingSpecialist.finished = true;
            mc.inSystem.remove(seeingSpecialist);
                        
            // Reactivate customer (no longer occupies a stall).
            mc.stallsInUse.update(-1);
            seeingSpecialist.activate(); 
           
            // Pay the specialist's commission.
            mc.todaysCost.update(AutoBodyShop.SPECIALIST_COMMISSION);
         }
      }
   }
}