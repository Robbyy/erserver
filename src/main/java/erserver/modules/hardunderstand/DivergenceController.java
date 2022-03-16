package erserver.modules.hardunderstand;

import erserver.modules.dependencies.*;
import erserver.modules.dependencies.vendorpagersystem.PagerSystem;
import erserver.modules.dependencies.vendorpagersystem.PagerTransport;
import erserver.modules.testtypes.Patient;

import java.util.List;

public class DivergenceController {

	private static final String ADMIN_ON_CALL_DEVICE = "111-111-1111";
	
	private boolean redDivergence;
	private boolean yellowDivergence;
	private boolean greenDivergence;
	private int redCount;
	private int yellowCount;
	private int greenCount;
	private int allowedCount;
	private int redBedOverflowAllowed;
	private int yellowBedOverflowAllowed;
	private int greenBedOverflowAllowed;

	public DivergenceController() {
		this.redDivergence = false;
		this.yellowDivergence = false;
		this.greenDivergence = false;
		this.redCount = 0;
		this.yellowCount = 0;
		this.greenCount = 0;
		this.allowedCount = 3;
		this.redBedOverflowAllowed = 0;
		this.yellowBedOverflowAllowed = 1;
		this.greenBedOverflowAllowed = 4;
	}

	public void check() {
      StaffAssignmentManager manager = new ERServerMainController().getStaffAssignmentManager();
      InboundPatientController controller = new ERServerMainController().getInboundPatientController();
      int[] redStaffRequired = {1, 2};
      int[] yellowStaffRequired = {1, 1};
      int[] greenStaffRequired = {0, 1};
      boolean redIncremented = false;
      boolean yellowIncremented = false;
      boolean greenIncremented = false;
      List<Patient> patients = controller.currentInboundPatients();
      List<Staff> staff = manager.getAvailableStaff();
      List<Bed> beds = manager.getAvailableBeds();
      int redInboundCount = 0;
      int yellowInboundCount = 0;
      int greenInboundCount = 0;
      int[] availableStaff = {0, 0};

      int criticalBedsAvailable = calculateCriticalBedsAvailable(beds);
      
      for (Patient patient : patients) {
         if (Priority.RED.equals(patient.getPriority())) {
            redInboundCount++;
         }
         else if (Priority.YELLOW.equals(patient.getPriority())) {
            yellowInboundCount ++;
         }
         else if (Priority.GREEN.equals(patient.getPriority())) {
            greenInboundCount ++;
         }
      }
      for (Staff cur : staff) {
         if (StaffRole.DOCTOR.equals(cur.getRole())) {
            availableStaff[0]++;
         }
         else if (StaffRole.NURSE.equals(cur.getRole())) {
            availableStaff[1]++;
         }
      }
      if (redInboundCount > (criticalBedsAvailable + redBedOverflowAllowed)) {
         redCount++;
         redIncremented = true;
      }
      if (yellowInboundCount + greenInboundCount > (beds.size() - criticalBedsAvailable + yellowBedOverflowAllowed + greenBedOverflowAllowed)) {
         if ( (greenInboundCount > (beds.size() - criticalBedsAvailable + greenBedOverflowAllowed)) && (yellowInboundCount <= (beds.size() - criticalBedsAvailable + yellowBedOverflowAllowed)) ) {
            greenCount++;
            greenIncremented = true;
         } else {
            greenCount++;
            yellowCount++;
            greenIncremented = true;
            yellowIncremented = true;
         }
      }
      
      int[] neededStaff = calculateNeededStaff(redStaffRequired, yellowStaffRequired, greenStaffRequired, redInboundCount,
			yellowInboundCount, greenInboundCount);
       
      if (neededStaff[0] > availableStaff[0]) {
         int diff = neededStaff[0] - availableStaff[0];
         if ((greenInboundCount * greenStaffRequired[0]) >= diff)  {
            if (!greenIncremented) {
               greenIncremented = true;
               greenCount++;
            }
         }
         else {
            int both = (yellowInboundCount * yellowStaffRequired[0]) + (greenInboundCount * greenStaffRequired[0]);
            if (both >= diff) {
               if (!greenIncremented) {
                  greenIncremented = true;
                  greenCount++;
               }
               if (!yellowIncremented) {
                  yellowIncremented = true;
                  yellowCount++;
               }
            }
            else {
               if (!greenIncremented) {
                  greenIncremented = true;
                  greenCount++;
               }
               if (!yellowIncremented) {
                  yellowIncremented = true;
                  yellowCount++;
               }
               if (!redIncremented) {
                  redIncremented = true;
                  redCount++;
               }
            }
         }
      }
      if (neededStaff[1] > availableStaff[1]) {
         int diff = neededStaff[1] - availableStaff[1];
         if ((greenInboundCount * greenStaffRequired[1]) >= diff)  {
            if (!greenIncremented) {
               greenIncremented = true;
               greenCount++;
            }
         }
         else {
            int both = (yellowInboundCount * yellowStaffRequired[1]) + (greenInboundCount * greenStaffRequired[1]);
            if (both >= diff) {
               if (!greenIncremented) {
                  greenIncremented = true;
                  greenCount++;
               }
               if (!yellowIncremented) {
                  yellowIncremented = true;
                  yellowCount++;
               }
            }
            else {
               if (!greenIncremented) {
                  greenIncremented = true;
                  greenCount++;
               }
               if (!yellowIncremented) {
                  yellowIncremented = true;
                  yellowCount++;
               }
               if (!redIncremented) {
                  redIncremented = true;
                  redCount++;
               }
            }
         }
      }
      EmergencyResponseService transportService = new EmergencyResponseService("http://localhost", 4567, 1000);
      if (redIncremented) {
         if ((redCount > allowedCount) && !redDivergence) {
            redDivergence = true;
            transportService.requestInboundDiversion(Priority.RED);
            sendDivergencePage("Entered divergence for RED priority patients!", true);
            redCount = 0;
         }
      } else {
         redCount = 0;
         if (redDivergence) {
            transportService.removeInboundDiversion(Priority.RED);
            sendDivergencePage("Ended divergence for RED priority patients.", false);
            redDivergence = false;
         }
      }
      if (yellowIncremented) {
         if ((yellowCount > allowedCount) && !yellowDivergence) {
            yellowDivergence = true;
            transportService.requestInboundDiversion(Priority.YELLOW);
            sendDivergencePage("Entered divergence for YELLOW priority patients!", true);
            yellowCount = 0;
         }
      } else {
         yellowCount = 0;
         if (yellowDivergence) {
            transportService.removeInboundDiversion(Priority.YELLOW);
            sendDivergencePage("Ended divergence for YELLOW priority patients.", false);
            yellowDivergence = false;
         }
      }
      if (greenIncremented) {
         if ((greenCount > allowedCount) && !greenDivergence) {
            greenDivergence = true;
            transportService.requestInboundDiversion(Priority.GREEN);
            sendDivergencePage("Entered divergence for GREEN priority patients!", true);
            greenCount = 0;
         }
      } else {
         greenCount = 0;
         if (greenDivergence) {
            transportService.removeInboundDiversion(Priority.GREEN);
            sendDivergencePage("Ended divergence for GREEN priority patients.", false);
            greenDivergence = false;
         }
      }
	}

	private int[] calculateNeededStaff(int[] redStaffRequired, int[] yellowStaffRequired, int[] greenStaffRequired,
			int redInboundCount, int yellowInboundCount, int greenInboundCount) {
		int[] neededStaff = {0, 0};      
		  neededStaff[0] = redInboundCount * redStaffRequired[0];
		  neededStaff[0] += yellowInboundCount * yellowStaffRequired[0];
		  neededStaff[0] += greenInboundCount * greenStaffRequired[0];
		  neededStaff[1] = redInboundCount * redStaffRequired[1];
		  neededStaff[1] += yellowInboundCount * yellowStaffRequired[1];
		  neededStaff[1] += greenInboundCount * greenStaffRequired[1];
		return neededStaff;
	}

	private int calculateCriticalBedsAvailable(List<Bed> beds) {
		int criticalBedsAvailable = 0;      
		for (Bed bed : beds) {
			if (bed.isCriticalCare()) {
				criticalBedsAvailable ++;
			}
		}
		return criticalBedsAvailable;
	}

	private void sendDivergencePage(String text, boolean requireAck) {
		try {
			PagerTransport transport = PagerSystem.getTransport();
			transport.initialize();
			if (requireAck) {
				transport.transmitRequiringAcknowledgement(ADMIN_ON_CALL_DEVICE, text);
			} else {
				transport.transmit(ADMIN_ON_CALL_DEVICE, text);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
