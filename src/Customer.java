/**
 * @author Riley Radle
 * 
 * Description: 
 *    This class models a process oriented 
 *    perspective of a customer entity within 
 *    the Auto Body Shop model. 
 * 
 * Last Edited: August 2021
 */

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class Customer extends SimProcess
{
   protected double arrivalTime;
   protected boolean finished;

   /**
    * @param owner
    * @param name
    * @param showInTrace
    * @param arrivalTime
    */
   public Customer(Model owner, String name, boolean showInTrace, double arrivalTime) 
   {
      super(owner, name, showInTrace);
      this.arrivalTime = arrivalTime;
   }

   @Override
   /**
    * Models the life cycle of a customer within the Auto Body Shop:
    *   1. Enters Auto Shop
    *   2. May or may not balk (depending on the number of cars currently there)
    *   3. Waits for one of the mechanics 
    *   4. Sees mechanic (May get referred to specialist, or be sent to another shop)
    *   5. Potentially Waits for Specialist
    *   6. Sees specialist
    *   7. Drives away
    */
   public void lifeCycle() throws SuspendExecution
   {
      // Loop will run max of 1 time (Used for break functionality).
      while (this.finished == false)
      {
         // Initializations and stat updates.
         AutoBodyShop abs = (AutoBodyShop)getModel();
         abs.totalCustomers.update(); // += 1
         abs.inSystem.insert(this);
         
         // ~~~~~~~~~~~~ Mechanic Logic ~~~~~~~~~~~~
         
         // Place the customer into the mechanic's queue.
         abs.waitingForMechanic.insert(this);
                 
         // There is a mechanic available.
         if (!abs.idleMechanics.isEmpty())
         {
            // Get mechanic and activate.
            Mechanic mechanic = abs.idleMechanics.removeFirst();
            mechanic.activate();
         }
         // There is not a mechanic available.
         else
         {
            // Determine if the customer will balk 
            // (subtract 1 so the customer doesn't count themself).
            double k = abs.waitingForMechanic.length() - 1;
            long balk = abs.balkDeterminer.sample();
                        
            // Customer leaves to the other shop.
            if (balk <= k)
            {
               // Set to finished and remove from queue.
               this.finished = true;
               abs.waitingForMechanic.remove(this);
                              
               // Update stats 
               abs.totalBalked.update(); // += 1
               abs.todaysCost.update(AutoBodyShop.LOSS_COST);
            }
            // Else: customer doesn't balk. 
         }
         
         // If the customer balked break from the loop.
         if (this.finished)
         {
            abs.inSystem.remove(this);
            break;
         }
         
         this.passivate();
         
         // If customer finished their service when with mechanic -> break.
         if (this.finished)
         {
            abs.inSystem.remove(this);
            break;
         }
         
         // ~~~~~~~~~~~~ Specialist Logic ~~~~~~~~~~~~
         
         // All of the stalls are full.
         if (abs.stallsInUse.getValue() >= AutoBodyShop.NUM_STALLS)  
         {
            this.finished = true;
            abs.waitingForSpecialist.remove(this);
                        
            // Update stats 
            abs.totalLost.update(); // += 1
            abs.todaysCost.update(AutoBodyShop.LOSS_COST);
         }
         // At least 1 available stall.
         else
         {
            // Add car to queue (it will occupy a stall).
            abs.stallsInUse.update(); // += 1
           
            // There is a specialist available.
            if (!abs.idleSpecialists.isEmpty())
            {
               // Get specialist and activate.
               Specialist specialist = abs.idleSpecialists.removeFirst();
               specialist.activate();
            }
         }
         
         // If all stalls were taken, break from the loop.
         if (this.finished)
         {
            abs.inSystem.remove(this);
            break;
         }
         
         this.passivate();
         
         // ~~~~~~~ Fully Treated at this Auto Body Shop ~~~~~~~
         // ~~~~~~~ Stats updated in specialist class ~~~~~~~   
      }      
   }
}
