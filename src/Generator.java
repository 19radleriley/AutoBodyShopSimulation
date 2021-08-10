package Simulation;
/**
 * @author Riley Radle
 * 
 * Description: 
 *    This class allows for automatic
 *    generation of new car arrivals.  
 *    (Permanent Entity)
 *    
 * Last Edited: August 2021
 */

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class Generator extends SimProcess 
{
   /**
    * @param owner
    * @param name
    * @param showInTrace
    */
   public Generator(Model owner, String name, boolean showInTrace) 
   {
      super(owner, name, showInTrace);
   }

   @Override
   /**
    * Generates new customers based on random arrival times.
    * The distributions of these arrival times changes as
    * time progresses in the Auto Body Shop model. 
    */
   public void lifeCycle() throws SuspendExecution
   {
      AutoBodyShop abs = (AutoBodyShop)getModel();
      
      // Generate new arrivals for the time the shop is open.
      while (abs.presentTime().getTimeAsDouble() < AutoBodyShop.OPERATION_HOURS)
      {
         // Determine the next interarrival time based on the time of day.
         double present = abs.presentTime().getTimeAsDouble();
         double time = 0;
         
         // 8 - 10
         if (present < 2) 
            time = abs.interarrivalTimes1.sample();
            
         // 10 - 4
         else if (present < 8)
            time = abs.interarrivalTimes2.sample();

         // 4 - 8
         else if (present < 12) 
            time = abs.interarrivalTimes3.sample();

         // Hold for the next arrival
         this.hold(new TimeSpan(time));
         
         // Next car arrives -> Activate
         Customer nextCar = new Customer(abs, "New Customer", true, abs.presentTime().getTimeAsDouble());
         nextCar.activate();
      }      
   }
}
