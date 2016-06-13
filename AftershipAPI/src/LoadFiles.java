import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;

import Classes.ConnectionAPI;
import Classes.Tracking;
import Enums.ISO3Country;

/**
 * Created by User on 17/6/14.
 */
/**
 * @author suhas
 *
 */
public class LoadFiles {

	static JLabel label;
	static JLabel labelUpdate;
	
    /**
     * Initializes JFrame and buttons with the appropriate ActionListeners
     * 
     * @param args
     */
    public static void main( String args[]){

		  JFrame frame = new JFrame("Aftership Uploader");
		  frame.setVisible(true);
		  frame.setSize(550,100);
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  
		  
		  JPanel panel = new JPanel();
		  frame.add(panel);
		  
		  label = new JLabel("C:/Users/CS/Desktop/TrackingNumbers.csv");
		  panel.add(label);
		  
		  JButton button1 = new JButton("Change dir");
		  panel.add(button1);
		  button1.addActionListener (new ActionChange());
		  
		  JButton button = new JButton("Upload");
		  panel.add(button);
		  button.addActionListener (new ActionUpload());
		  
		  labelUpdate = new JLabel("	");
		  panel.add(labelUpdate);
		  
		
    }
    
    /**
     * @author suhas
     *
     * Actionlistener to upload all trackings from Shipworks CSV file to
     * Aftership
     */
    static class ActionUpload implements ActionListener {        
    	  public void actionPerformed (ActionEvent a) {
    		
    		//Placeholder API key used here
			ConnectionAPI connection = new ConnectionAPI("x");
			//Initial file directory
			String csvFile = label.getText();
			BufferedReader br = null;
			String line = "";
			//Character to split lists by
			String cvsSplitBy = ",";

			//Reads from csv file and uploads
			
			try {
				
				br = new BufferedReader(new FileReader(csvFile));
				int lines = 0;
				while ((line = br.readLine()) != null) {
					//Ignore first line
					if (lines != 0) {
						
						//Initialize tracking and variables
						Tracking tracking1;
						String orderID = null;
						String trackingNumber = null;
						int innerLines = 0;
						//For all non-ASG orders:
						if (line.charAt(0) != 'A') {
							//Every package's information is on three lines, reads
							//information by sets of three lines
							while (innerLines < 3 && line != null) {
								// use comma as separator
								String[] trackingLine = line.split(cvsSplitBy);
								if (innerLines == 0) {
									orderID = trackingLine[0];
									System.out.println(orderID);
								} else if (innerLines == 2) {
									trackingNumber = trackingLine[3];
									System.out.println(trackingNumber);
								}
								if (innerLines != 2) {
									line = br.readLine();
								}
								innerLines = innerLines + 1;
							}
						//ASG orders are on one lines:
						} else {
							String[] trackingLine = line.split(cvsSplitBy);
							orderID = trackingLine[0];
							System.out.println(orderID);
							trackingNumber = trackingLine[3];
							System.out.println(trackingNumber);
						}
						//Give tracking the necessary parameteres
						tracking1 = new Tracking(trackingNumber);
						tracking1.setOrderID(orderID);

						System.out.println(tracking1);
						//Upload
						try {
							Tracking trackingPosted = connection.postTracking(tracking1);
						} catch (Exception e) {
							System.out.println(e);
						}

					}
					lines = lines + 1;

				}
				labelUpdate.setText("Done");

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				labelUpdate.setText("File Not Found");
			} catch (IOException e) {
				e.printStackTrace();
				labelUpdate.setText("Bad File");
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			
		}
    }   


    /**
     * @author suhas
     * 
     * ActionListener to change the directory of the csv file being uploaded.
     * Opens JFileChooser
     */
    static class ActionChange implements ActionListener {        
  	  public void actionPerformed (ActionEvent a) {  
  		JFileChooser chooser = new JFileChooser();
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Choose File");
	    chooser.setAcceptAllFileFilterUsed(false);
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
	    chooser.setFileFilter(filter);

	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	      label.setText(chooser.getSelectedFile().toString());
	      labelUpdate.setText("	");
	    } else {
	      System.out.println("No Selection ");
	    }
  	  }
  	}

    public void loadFile()throws IOException, JSONException{
        BufferedReader br =
                new BufferedReader( new InputStreamReader(new FileInputStream("../TrackingsToAdd.txt"), "UTF8"));
        ConnectionAPI connection  = new ConnectionAPI("a61d6204-6477-4f6d-93ec-86c4f872fb6b");
        try {
            String line;
            String [] elements;

            int added =0;
            Tracking newTracking;
            while ((line = br.readLine()) != null)
            {

//              slug, smses11, emails10, title5, customerName6, orderID7, orderIDPath8,
//              destinationCountryISO3 27
                elements=line.split("\t");
                // System.out.println("**long "+elements.length + "    number "+elements[2]);
                newTracking = new Tracking(elements[2]);
                newTracking.setSlug(elements[4]);
                if(5<elements.length && !elements[5].equals("")) newTracking.setTitle(elements[5]);
                if(6<elements.length && !elements[6].equals("")) newTracking.setCustomerName(elements[6]);
                if(7<elements.length &&!elements[7].equals("")) newTracking.setOrderID(elements[7]);
                if(8<elements.length &&!elements[8].equals("")) newTracking.setOrderIDPath(elements[8]);
                if(27<elements.length &&!elements[27].equals("")) newTracking.setDestinationCountryISO3(ISO3Country.valueOf(elements[27]));
                if(11<elements.length &&!elements[11].equals("")){
                    String smses[] = elements[11].split(",");
                    for(int i=0;i<smses.length;i++)
                        newTracking.addSmses(smses[i]);
                }
                if(10<elements.length &&!elements[10].equals("")){
                    String emails[] = elements[10].split(",");
                    for(int i=0;i<emails.length;i++)
                        newTracking.addEmails(emails[i]);
                }
                System.out.println(newTracking.generateJSON());
                try {
                    connection.postTracking(newTracking);
                    added++;
                    System.out.println("added " + added);
                }catch(Exception e){
                    System.out.println(e.getMessage()+"   number"+ elements[2]);
                }


            }
        } finally {
            br.close();
        }
    }
}

//order elements in file
//            System.out.println("created_at" +elements[0]+
//                    "\tupdated_at" +elements[1]+
//                    "\ttracking_number" +elements[2]+
//                    "\ttags" +elements[3]+
//                    "\tcourier" +elements[4]+
//                    "\ttitle" +elements[5]+
//                    "\tcustomer_name" +elements[6]+
//                    "\torder_id" +elements[7]+
//                    "\torder_id_path" +elements[8]+
//                    "\tsource" +elements[9]+
//                    "\temails" +elements[10]+
//                    "\tsmses" +elements[11]+
//                    "\tshipment_type" +elements[12]+
//                    "\tshipment_weight" +elements[13]+
//                    "\tshipment_package_count" +elements[14]+
//                    "\tshipment_pickup_date" +elements[15]+
//                    "\tshipment_scheduled_delivery_date" +elements[16]+
//                    "\tshipment_delivery_date" +elements[17]+
//                    "\tsigned_by" +elements[18]+
//                    "\torigin_address" +elements[19]+
//                    "\torigin_country_name" +elements[20]+
//                    "\torigin_country_iso3" +elements[21]+
//                    "\torigin_state" +elements[22]+
//                    "\torigin_city" +elements[23]+
//                    "\torigin_zip" +elements[24]+
//                    "\tdestination_address" +elements[25]+
//                    "\tdestination_country_name" +elements[26]+
//                    "\tdestination_country_iso3" +elements[27]+
//                    "\tdestination_state" +elements[28]+
//                    "\tdestination_city" +elements[29]+
//                    "\tdestination_zip");