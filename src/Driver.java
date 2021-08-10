/**
 * @author: Riley Radle
 * 
 * Description: 
 *    This class is the driver that allows the simulation 
 *    to be run by the user.  It sets up graphical components
 *    which the user to can configure to alter the simulation
 *    and observe different results.
 * 
 * Last Edited: August 2021
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Driver implements ActionListener
{
   /** Initiates the program */
   public static void main(String[] args)
   {
      new Driver();
   }
   
   /** Graphical Components */
   private JFrame frame;
   private JButton runSimulation;
   private JPanel container;
   private JCheckBox outputPerRep;

   /** For Running the Simulation */
   private ReplicationModel simulation;
   
   /** All of the dropdown options */
   private JComboBox<Integer> operationHours;
   private JComboBox<Integer> stallCost;
   private JComboBox<Integer> lossCost;
   private JComboBox<Integer> numMechanics;
   private JComboBox<Double> mechanicCommission;
   private JComboBox<Double> mechanicSalary;
   
   private JComboBox<Integer> numSpecialists;
   private JComboBox<Integer> numStalls;
   private JComboBox<Double> specialistCommission;
   private JComboBox<Double> specialistSalary;
   private JComboBox<Integer> numReplications;
   
   
   /**
    * Constructor sets up a JFrame where the
    * user is able to configure the simulation variables.
    */
   public Driver() 
   {  
      // Initialize the ReplicationModel for running the simulation.
      simulation = new ReplicationModel(null, "ABS", true, true);

      // Set up the main JFrame
      frame = new JFrame("Auto Body Simulation");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      // Make the frame appear in the center of the user's screen 
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setLocation((int)screenSize.getWidth() / 2 - (1000 / 2), 0);
      
      // Set up the main container for the JFrame
      container = new JPanel();
      container.setPreferredSize(new Dimension(1000, 200));
      container.setLayout(new GridLayout(2, 6));
      
      // Add all of the dropdown menus
      addDropdowns();
      
      // Add one more checkbox
      outputPerRep = new JCheckBox("Output Per Rep");
      outputPerRep.addActionListener(this);
      container.add(outputPerRep);
      
      runSimulation = new JButton("Run Simulation");
      runSimulation.setFont(new Font("Calibri", Font.PLAIN, 20));
      runSimulation.addActionListener(this);
     
      // Add all of the components for display.
      frame.add(container);
      frame.add(runSimulation, BorderLayout.SOUTH);
      frame.pack();
      frame.setVisible(true);
   }

   @Override
   /**
    * This method handles the assignment of 
    * the multiple simulation variables through the 
    * use of JComboBoxes. It also handles the action of 
    * starting the full simulation.
    */
   public void actionPerformed(ActionEvent e) 
   {
      // Button for running simulation was clicked
      if (e.getSource() == runSimulation)
      {
         ReplicationModel.NUM_REPLICATIONS = (int)numReplications.getSelectedItem();
         ReplicationModel.INCLUDE_OUTPUT_PER_REPLICATION = outputPerRep.isSelected();
         simulation.runFullSimulation();
      }
      // One of the drop boxes was updated
      else if (e.getSource() == operationHours)
         AutoBodyShop.OPERATION_HOURS = (int)operationHours.getSelectedItem();
      else if (e.getSource() == stallCost)
         AutoBodyShop.STALL_COST = (int)stallCost.getSelectedItem();
      else if (e.getSource() == lossCost)
         AutoBodyShop.LOSS_COST = (int)lossCost.getSelectedItem();
      else if (e.getSource() == numMechanics)
         AutoBodyShop.NUM_MECHANICS = (int)numMechanics.getSelectedItem();
      else if (e.getSource() == mechanicCommission)
         AutoBodyShop.MECHANIC_COMMISSION = (double)mechanicCommission.getSelectedItem();
      else if (e.getSource() == mechanicSalary)
         AutoBodyShop.MECHANIC_SALARY = (double)mechanicSalary.getSelectedItem();
      else if (e.getSource() == numSpecialists)
         AutoBodyShop.NUM_SPECIALISTS = (int)numSpecialists.getSelectedItem();
      else if (e.getSource() == numStalls)
         AutoBodyShop.NUM_STALLS = (int)numStalls.getSelectedItem();
      else if (e.getSource() == specialistCommission)
         AutoBodyShop.SPECIALIST_COMMISSION = (double)specialistCommission.getSelectedItem();
      else if (e.getSource() == specialistSalary)
         AutoBodyShop.SPECIALIST_SALARY = (double)specialistSalary.getSelectedItem();
   }
   
   /** Methods for adding dropdowns and menu bars to the JFrame */
   
   @SuppressWarnings("unchecked")
   /**
    * This method sets up the multiple drop down menus that allow
    * the user to configure the simulation variables.
    */
   private void addDropdowns()
   {
      operationHours = (JComboBox<Integer>)addDropdown(
            "Operation Hours", new Integer[]{6, 7, 8, 9, 10, 11, 12});
      stallCost = (JComboBox<Integer>)addDropdown(
            "Cost Per Stall", new Integer[]{100, 200, 300, 400, 500});
      lossCost = (JComboBox<Integer>)addDropdown(
            "Customer Loss Cost", new Integer[]{400, 500, 600, 700});
      numMechanics = (JComboBox<Integer>)addDropdown(
            "Number of Mechanics", new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
      mechanicCommission = (JComboBox<Double>)addDropdown(
            "Mechanic Commission", new Double[]{10d, 20d, 30d, 40d, 50d});
      mechanicSalary = (JComboBox<Double>)addDropdown(
            "Mechanic Salary", new Double[]{100d, 200d, 300d});
      numSpecialists = (JComboBox<Integer>)addDropdown(
            "Number of Specialists", new Integer[]{1, 2, 3, 4, 5});
      numStalls = (JComboBox<Integer>)addDropdown(
            "Number of Specialist Stalls", new Integer[]{1, 2, 3, 4, 5});
      specialistCommission = (JComboBox<Double>)addDropdown(
            "Specialist Commission", new Double[]{50d, 60d, 70d, 80d, 90d, 100d});
      specialistSalary = (JComboBox<Double>)addDropdown(
            "Specialist Salary", new Double[]{300d, 400d, 500d});
      numReplications = (JComboBox<Integer>)addDropdown(
            "Simulation Replications", new Integer[]{100, 200, 300, 400, 500, 600, 700, 800, 900, 1000});
      numReplications.setEditable(true);
   }
   
   /**
    * This is a helper method that sets up an individual
    * drop down menu.  It is used to reduce repeated code.
    * 
    * @param name : The title to display above the drop down menu.
    * @param options : The various options within the drop down that can be selected.
    * 
    * @return : The newly configured drop down menu (so it can be initialized)
    */
   private JComboBox<?> addDropdown(String name, Object[] options)
   {
      // Set up a container for the drop down 
      JPanel pane = new JPanel();
      
      // Set up and add the drop down to the container
      JComboBox<Object> dropDown = new JComboBox<>(options);
      dropDown.addActionListener(this);
      JLabel text = new JLabel(name);
      
      pane.add(text);
      pane.add(dropDown);
      container.add(pane);
      
      return dropDown;
   }

}
