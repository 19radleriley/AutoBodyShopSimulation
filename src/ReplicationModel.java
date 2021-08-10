package Simulation;
/**
 * @author Riley Radle
 * 
 * Description: 
 *    This class provides an automatic mechanism
 *    for multiple runs of the AutoBodyShop simulation.
 *    Several data values are recorded in confidence 
 *    calculators which generate 95% confidence intervals for
 *    several statistics. 
 * 
 * Last Edited: August 2021
 */

import java.util.concurrent.TimeUnit;
import desmoj.core.simulator.*;
import desmoj.core.statistic.*;
import java.awt.*;
import javax.swing.*;

public class ReplicationModel extends Model
{
   /** Config Variables and Statistical Trackers */
   public static int NUM_REPLICATIONS = 100;
   public static boolean INCLUDE_OUTPUT_PER_REPLICATION = true;
   
   /** Replication model trackers */
   protected ConfidenceCalculator dailyOperatingCost;
   protected ConfidenceCalculator avgTotalCustomers;
   protected ConfidenceCalculator avgBalkCustomers;
   protected ConfidenceCalculator avgLostCustomers;
   protected ConfidenceCalculator avgFullyFixedCustomers;
   protected ConfidenceCalculator avgResponseTime;
   protected ConfidenceCalculator mechanicUtilRate;
   protected ConfidenceCalculator specialistUtilRate;
   protected ConfidenceCalculator avgWaitingForMechanic;
   
   /** Variables for displaying results */
   JFrame finalResults;
   JFrame repResults;
   JLabel textBox;
   JScrollPane scrollPane;
   String repInformation = "<html><pre>";
   
   /**
    * Constructor sets up the model 
    * 
    * @param owner
    * @param name
    * @param showInTrace
    * @param showInReport
    */
   public ReplicationModel(Model owner, String name, boolean showInTrace, boolean showInReport) 
   {
      super(owner, name, showInTrace, showInReport); 
   } 

   @Override
   /**
    * Return a short description of the model.
    */
   public String description() 
   {
      return "A replication model for running AutoBodyShop many times";
   }

   @Override
   /**
    * This method runs the AutoBodyShop Simulation the selected number of times.
    */
   public void doInitialSchedules() 
   {
      if (INCLUDE_OUTPUT_PER_REPLICATION) 
      {
         // Set up the categories for per-rep information
         repInformation += String.format( "        Daily   Total      Balked     Lost       Fully Fixed     Response     Waiting<br>"
                                        + "Repl.#  Cost    Customers  Customers  Customers  Cars            Time (avg)   Time (AVG)<br>"
                                        + "--------------------------------------------------------------------------------------------<br>");
      }
      
      boolean noErrors = true;
      
      // Run the replications
      for (int i = 1; i <= NUM_REPLICATIONS; ++i) 
      {
         noErrors = runSimulation(i);
         
         // If there was an error display it to the user and stop the simulation.
         if (!noErrors) 
         {
             JOptionPane.showMessageDialog(null, "Error running simulation. \n Please Rerun simulation.", 
                                           "Error", JOptionPane.ERROR_MESSAGE);
             break;
         }
      }

      // Display the final results of the simulation
      // across all of the repetitions. 
      if (noErrors)
      {
         displayFinalResults();
         if (INCLUDE_OUTPUT_PER_REPLICATION)
            displayRepResults();
      }
   }
  
   /**
    * Run the simulation model a single time. 
    * 
    * @return : True if the simulation ran correctly.
    */
   public boolean runSimulation(int runNumber) 
   { 
      // Create an instance of the AutoBodyShop Model
      AutoBodyShop abs = new AutoBodyShop(null, "Auto Body Shop", true, true);

      Experiment exp = new Experiment("Single Run");
      
      // Set the seed for the random number generator
      // (NOTE: Do this *before* connecting the experiment to the model)
      exp.setSeedGenerator(979 + 2*runNumber);

      // Connect model and experiment
      abs.connectToExperiment(exp);

      // Set experiment parameters
      exp.setShowProgressBar(false);
      
      // ~~~~~~~~~~~~~~~ Create the custom stopping condition for the experiment ~~~~~~~~~~~~~~~ 
      class Stop extends ModelCondition 
      {

         public Stop(Model owner, String name, boolean showInTrace) 
         {
            super(owner, name, showInTrace);
         }

         @Override
         public boolean check() 
         {
            boolean pastClosing = abs.presentTime().getTimeAsDouble()
                                 > AutoBodyShop.OPERATION_HOURS;
            
            boolean noMoreCustomers = abs.inSystem.isEmpty();
            
            return pastClosing && noMoreCustomers;
         } 
      }
      // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 

      exp.stop(new Stop(abs, "Stopping Condition", true));
      exp.traceOff(new TimeInstant(0));
      exp.debugOff(new TimeInstant(0));
      exp.setSilent(true);

      // Run experiment and gather output as needed
      try 
      {
          exp.start();
      } 
      catch (Exception e)
      {
         // Return that there was an error
         exp.finish();
         return false;
      }

      // Check for errors or problems during the run
      if (exp.hasError() || exp.isAborted()) 
      {
         // Return that there was an error
         exp.finish();
         return false;
      }

      // If experiment stopped without errors, sleep for a bit before
      // generating the report and finishing things off. This 
      // significantly reduces the occurrence of threading issues over
      // repeated replications.
      try 
      {
          Thread.sleep(10);
      } 
      catch (InterruptedException e)
      {
          // Do nothing
      }

      // Generate report as needed, then stop and close all output files
      exp.report();
      exp.finish();

      // Get results from simulation run
      double todaysCost = abs.todaysCost.getValue();
      long totalCustomers = abs.totalCustomers.getValue();
      long totalBalked = abs.totalBalked.getValue();
      long totalLost = abs.totalLost.getValue();
      long fullyFixed = abs.fullyFixed.getValue();
      double responseTime = abs.responseTimes.getMean();
      double avgCustomersWaiting = abs.waitingForMechanic.averageLength();

      // Get the utilization rates.
      double mechanicUtil = abs.idleMechanics.maxLength() - abs.idleMechanics.averageLength();
      mechanicUtil = mechanicUtil / abs.idleMechanics.maxLength();
      double specUtil = abs.idleSpecialists.maxLength() - abs.idleSpecialists.averageLength();
      specUtil = specUtil / abs.idleSpecialists.maxLength();
      
      // Lastly, check for bad values in output to prevent them from
      // corrupting the aggregate replication results.
      if (abs.presentTime().getTimeAsDouble() < 6   ||
              todaysCost < 0   || totalCustomers < 0 ||
              totalBalked < 0  || totalLost < 0       ||
              fullyFixed < 0 || responseTime < 0  || 
              mechanicUtil < 0    || specUtil     < 0  ||
              avgCustomersWaiting < 0) 
      {
         // Return that there was an error
         return false;
      }

      // Update the replication model statistics with replication results
      dailyOperatingCost.update(todaysCost);
      avgTotalCustomers.update(totalCustomers);
      avgBalkCustomers.update(totalBalked);
      avgLostCustomers.update(totalLost);
      avgFullyFixedCustomers.update(fullyFixed);
      avgResponseTime.update(responseTime);
      avgWaitingForMechanic.update(avgCustomersWaiting);
      mechanicUtilRate.update(mechanicUtil);
      specialistUtilRate.update(specUtil);
      
      // Update the results from simulation run to be displayed.
      if (INCLUDE_OUTPUT_PER_REPLICATION) 
      {
         repInformation += String.format("%6d: %-6.0f %-9d  %-9d  %-9d  %-14d  %-12.3f %-15.3f<br/>",
                           runNumber, todaysCost, totalCustomers, totalBalked,
                           totalLost, fullyFixed, responseTime, avgCustomersWaiting);
      }

      // Simulation rep finished without error
      return true;
   }

   @Override
   /**
    * Initialize all necessary trackers 
    * and variables for replication model.
    */
   public void init() 
   {
      // Initialize all statistical trackers. 
      dailyOperatingCost = new ConfidenceCalculator(this,
                "Daily Operating Cost", true, false);
      avgTotalCustomers = new ConfidenceCalculator(this, 
                "Average Total Customers", true, false);
      avgBalkCustomers = new ConfidenceCalculator(this,
                        "Average Balked Customers", true, false); 
      avgLostCustomers = new ConfidenceCalculator(this, 
                        "Average Lost Customers", true, false);
      avgFullyFixedCustomers = new ConfidenceCalculator(this,
                         "Average Fully Fixed", true, false);
      avgResponseTime = new ConfidenceCalculator(this,
                         "Average Response Time", true, false);
      avgWaitingForMechanic = new ConfidenceCalculator(this,
                         "Average in Waiting Room", true, false);
      mechanicUtilRate = new ConfidenceCalculator(this, 
                         "Mechanic Utilization Rate", true, false);
      specialistUtilRate = new ConfidenceCalculator(this, 
            "Specialist Utilization Rate", true, false);
   }
   
   /**
    * This method starts the ReplicationModel which will
    * then run the AutoBodyShop simulation the desired number
    * of times.  
    */
   public void runFullSimulation()
   {
      // Reference time for simulation is in hours
      Experiment.setReferenceUnit(TimeUnit.HOURS);

      // create the model and experiment and connect them
      ReplicationModel repModel = new ReplicationModel(null,
              "Replication Model for Auto Body Shop", true, true);
      Experiment exp = new Experiment("Auto Body Shop Exp");
      repModel.connectToExperiment(exp);

      // Set experiment parameters
      // Note that repModel runs single time
      // so the stopping condition is at time 0.
      exp.setShowProgressBar(false);
      exp.stop(new TimeInstant(0));
      exp.traceOff(new TimeInstant(0));
      exp.debugOff(new TimeInstant(0));
      exp.setSilent(true);

      // Start the experiment at simulation time 0.0
      exp.start();
 
      // Simulation is running ...

      // generate the report (and other output files)
      exp.report();

      // stop all threads still alive and close all output files
      exp.finish();
   }
   
   /**
    * This method sets up a JFrame to display the information
    * that was generated for each repetition of the AutoBodyShop
    * simulation.  
    */
   private void displayRepResults()
   {

      // Set up the JFrame for displaying information
      repResults = new JFrame("Output Per Repetition");
      repResults.setResizable(true);
      repResults.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      // Make the frame appear in the center of the user's screen
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      repResults.setLocation((int)screenSize.getWidth() / 2 - (1000 / 2), 530);
       
      // Set up components to go in JFrame
      textBox = new JLabel();
      textBox.setHorizontalAlignment(JLabel.CENTER);
      scrollPane = new JScrollPane(textBox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
      scrollPane.setPreferredSize(new Dimension(1000, 400));
      
      textBox.setText(repInformation);
      repResults.add(scrollPane);
      repResults.pack();
      repResults.setVisible(true);
   }
   
   /**
    * This method sets up a JFrame to display the final results
    * of the simulation across all repetitions of the AutoBodyShop model.
    */
   private void displayFinalResults()
   {
      // Set up JFrame for displaying final results
      finalResults = new JFrame("Output Across Repetitions");
      finalResults.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
      finalResults.setResizable(false);

      // Make the frame appear in the center of the user's screen 
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      finalResults.setLocation((int)screenSize.getWidth() / 2 - (1030 / 2), 0);
     
      // Set up container to hold the grid of information
      JPanel container = new JPanel();
      container.setLayout(null);
      container.setPreferredSize(new Dimension(1030, 430));
     
      // Set up containers for the columns of information
      JPanel namePane = new JPanel();
      namePane.setLayout(new GridLayout(10, 1));
      namePane.setBounds(15, 15, 200, 400);
      JPanel numberPane = new JPanel();
      numberPane.setLayout(new GridLayout(10, 8));
      numberPane.setBounds(215, 15, 800, 400);
     
      // Add the labels for the columns 
      namePane.add(new JLabel("TITLE"));
      numberPane.add(new JLabel("OBS"));
      numberPane.add(new JLabel("MEAN"));
      numberPane.add(new JLabel("STD. DEV"));
      numberPane.add(new JLabel("MIN"));
      numberPane.add(new JLabel("MAX"));
      numberPane.add(new JLabel("CONF. LEVEL"));
      numberPane.add(new JLabel("CONF. LOWER"));
      numberPane.add(new JLabel("CONF. UPPER"));
      
      // Add the information to the columns 
      displayRow(dailyOperatingCost, namePane, numberPane);
      displayRow(avgTotalCustomers, namePane, numberPane);
      displayRow(avgBalkCustomers, namePane, numberPane);
      displayRow(avgLostCustomers, namePane, numberPane);
      displayRow(avgFullyFixedCustomers, namePane, numberPane);
      displayRow(avgResponseTime, namePane, numberPane);
      displayRow(mechanicUtilRate, namePane, numberPane);
      displayRow(specialistUtilRate, namePane, numberPane);
      displayRow(avgWaitingForMechanic, namePane, numberPane);

      // Display all of the elements
      container.add(namePane);
      container.add(numberPane);
      finalResults.add(container);
      finalResults.pack();
      finalResults.setVisible(true);
   }
   
   /**
    * Helper method to add a row of information to the final 
    * results window.  This reduces repeated code.
    * 
    * @param cc : The source of the statistics for this row
    * @param name : The container that holds the name of the row
    * @param numbers : The container that holds the statistics for the row
    */
   private void displayRow(ConfidenceCalculator cc, JPanel name, JPanel numbers)
   {
      name.add(new JLabel(cc.getName()));
      numbers.add(new JLabel("" + cc.getObservations()));
      numbers.add(new JLabel("" + cc.getMean()));
      numbers.add(new JLabel("" + cc.getStdDev()));
      numbers.add(new JLabel("" + cc.getMinimum()));
      numbers.add(new JLabel("" + cc.getMaximum()));
      numbers.add(new JLabel("" + cc.getConfidenceLevel()));
      numbers.add(new JLabel("" + cc.getConfidenceIntervalOfMeanLowerBound()));
      numbers.add(new JLabel("" + cc.getConfidenceIntervalOfMeanUpperBound()));
   }
}