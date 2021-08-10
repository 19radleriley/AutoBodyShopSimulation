/**
 * @author Riley Radle
 * 
 * 
 * Description: 
 *    This class models an Auto Body Shop with a 
 *    specified number of mechanics, specialists, 
 *    and specialist stalls.  These values can 
 *    be changed to alter behavior of the simulation
 *    by updating the constants at the top of the class.
 * 
 * Last Edited: August 2021
 */

import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.*;

public class AutoBodyShop extends Model
{
   /** 
    * State variables and constants 
    * Driver changes these to change behavior of system
    */
   
   // Model constants and state variables.
   protected static int OPERATION_HOURS = 6;  
   protected static int STALL_COST = 100;
   protected static int LOSS_COST = 400;
   protected static final double INTERARRIVAL_8_10 = 15.0 / 60;
   protected static final double INTERARRIVAL_10_4 = 6.0 / 60;
   protected static final double INTERARRIVAL_4_8 = 9.0 / 60;
   
   // Mechanic constants.
   protected static int    NUM_MECHANICS = 1; 
   protected static double MECHANIC_SALARY = 100;       
   protected static double MECHANIC_COMMISSION = 10;
   protected static final double MECHANIC_FIX_TIME = 8.0 / 60;   
   protected static final double MECHANIC_REFER_RATE = .4;
   
   // Specialist constants.
   protected static int NUM_SPECIALISTS = 1;
   protected static int NUM_STALLS = 1;
   protected static double SPECIALIST_SALARY = 300; 
   protected static double SPECIALIST_COMMISSION = 100;  
   protected static final double SPECIALIST_FIX_TIME = 25.0 / 60;

  
   /** Sources of randomness  */
   protected ContDistExponential interarrivalTimes1; // 8-10
   protected ContDistExponential interarrivalTimes2; // 10-4
   protected ContDistExponential interarrivalTimes3; // 4-8
   
   protected ContDistExponential mechanicFixTimes;
   protected ContDistExponential specialistFixTimes;
  
   protected BoolDistBernoulli   mechanicReferral;
   protected DiscreteDistUniform balkDeterminer;
   
   /** Structures */
   protected ProcessQueue<Mechanic> idleMechanics;
   protected ProcessQueue<Specialist> idleSpecialists;
   protected ProcessQueue<Customer> waitingForMechanic;
   protected ProcessQueue<Customer> waitingForSpecialist;
   protected ProcessQueue<Customer> inSystem;
   
   /** Trackers */
   protected Count totalCustomers;
   protected Count totalBalked;
   protected Count totalLost;
   protected Count fullyFixed;   
   protected Count stallsInUse;
   protected Tally responseTimes;
   protected Aggregate todaysCost;
   
   /**
    * @param owner
    * @param name
    * @param showInReport
    * @param showInTrace
    */
   public AutoBodyShop(Model owner, String name, boolean showInReport, boolean showInTrace)
   {
      super(owner, "Auto Body Shop", showInReport, showInTrace);
   }

   @Override
   /**
    * Return a short description of the system this class models. 
    */
   public String description() 
   { 
      return "Model of an auto body shop with basic mechanics and specialists";
   }

   @Override
   /**
    * Creates all entities and begins interarrivals of customers (using generator). 
    */
   public void doInitialSchedules() 
   {
      // Create all mechanics.
      for (int i = 0; i < AutoBodyShop.NUM_MECHANICS; i++)
      {
         Mechanic mechanic = new Mechanic(
               this, "Mechanic", true, 
               AutoBodyShop.MECHANIC_SALARY, 
               AutoBodyShop.MECHANIC_COMMISSION,
               AutoBodyShop.MECHANIC_FIX_TIME, 
               AutoBodyShop.MECHANIC_REFER_RATE);
         
         this.idleMechanics.insert(mechanic);
         
         // Activate Life Cycle
         mechanic.activate();
      }
      
      // Create all specialists. 
      for (int i = 0; i < AutoBodyShop.NUM_SPECIALISTS; i++)
      {
         Specialist spec = new Specialist(
               this, "Specialist", true, 
               AutoBodyShop.SPECIALIST_SALARY, 
               AutoBodyShop.SPECIALIST_COMMISSION,
               AutoBodyShop.SPECIALIST_FIX_TIME);
         
        this.idleSpecialists.insert(spec); 
        
         // Activate Life Cycle
         spec.activate();
      }
     
      // Use generator to start arrivals.
      Generator gen = new Generator(this, "Generator", true);
      gen.activate();
      
      // Incur the cost of each specialist stall.
      this.todaysCost.update(AutoBodyShop.NUM_STALLS * AutoBodyShop.STALL_COST);
   }

   @Override
   /**
    * Initialize all relevant structures, state variables, and statistical trackers.
    */
   public void init() 
   {
      // Init Structures 
      idleMechanics = new ProcessQueue<>(this, "Idle Mechanic Queue", true, false);
      idleSpecialists = new ProcessQueue<>(this, "Idle Specialist Queue", true, false);      
      waitingForMechanic = new ProcessQueue<>(this, "Mechanic Waiting Queue", true, false);
      waitingForSpecialist = new ProcessQueue<>(this, "Specialist Waiting Queue", true, false);
      inSystem = new ProcessQueue<>(this, "Total People in Shop", true, false);
      
      // Init Trackers
      totalCustomers = new Count(this, "Total Customers", true, false);
      totalBalked = new Count(this, "Total Balked", true, false);
      totalLost = new Count(this, "Total Lost", true, false);
      fullyFixed = new Count(this, "Fully Fixed", true, false);
      stallsInUse = new Count(this, "Stalls in use", true, false);

      responseTimes = new Tally(this, "Response Times", true, false);
      todaysCost = new Aggregate(this, "Today's Cost", true, false); 
      
      // Init sources of randomness
      interarrivalTimes1 = new ContDistExponential(this, 
            "8-10 Interarrival Times", AutoBodyShop.INTERARRIVAL_8_10, true, false);
      interarrivalTimes2 = new ContDistExponential(this, 
            "10-4 Interarrival Times", AutoBodyShop.INTERARRIVAL_10_4, true, false);
      interarrivalTimes3 = new ContDistExponential(this, 
            "4-8 Interarrival Times", AutoBodyShop.INTERARRIVAL_4_8, true, false);
      mechanicFixTimes = new ContDistExponential(this,
            "Mechanic Fix Times", AutoBodyShop.MECHANIC_FIX_TIME,
            true, false);
      specialistFixTimes = new ContDistExponential(this,
            "Specialist Fix Times", AutoBodyShop.SPECIALIST_FIX_TIME,
            true, false);
      mechanicReferral = new BoolDistBernoulli(this, "Mechanic Referral",
            AutoBodyShop.MECHANIC_REFER_RATE, true, false);
      balkDeterminer = new DiscreteDistUniform(this, "Balk Determiner", 1, 8, true, false);
   }
}