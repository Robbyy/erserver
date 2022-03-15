package erserver.modules.testtypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DosingCalculatorTest {

    private DosingCalculator dosingCalculator;
    private Patient patient;

    @BeforeEach
    public void setUp() {
        dosingCalculator = new DosingCalculator();
        patient = new Patient();
    }

    @Test
    public void returnsCorrectDosesForNeonate() {
        patient.setBirthDate(LocalDate.now().minusDays(28));
        String singleDose = dosingCalculator.getRecommendedSingleDose(patient, "Tylenol Oral Suspension");
        assertEquals("0", singleDose);
    }

    @Test
    public void returnsCorrectDosesForInfant() {
        patient.setBirthDate(LocalDate.now().minusDays(40));
        String singleDose = dosingCalculator.getRecommendedSingleDose(patient, "Tylenol Oral Suspension");
        assertEquals("2.5 ml", singleDose);
    }

    @Test
    public void returnsCorrectDosesForChild() {
        patient.setBirthDate(LocalDate.now().minusYears(3));
        String singleDose = dosingCalculator.getRecommendedSingleDose(patient, "Tylenol Oral Suspension");
        assertEquals("5 ml", singleDose);
    }

    @Test
    public void returnsCorrectDosesForNeonateAmox() {
        patient.setBirthDate(LocalDate.now().minusDays(20));
        String singleDose = dosingCalculator.getRecommendedSingleDose(patient, "Amoxicillin Oral Suspension");
        assertEquals("15 mg/kg", singleDose);
    }

    @Test()
    public void returnsExceptionForAdults() 
    {
        assertThrows(RuntimeException.class, () -> exceptionForAdults(), "Expected exceptionForAdults() to throw, but it didn't");
    }

	private void exceptionForAdults()
	{
		patient.setBirthDate(LocalDate.now().minusYears(16));
        dosingCalculator.getRecommendedSingleDose(patient, "Amoxicillin Oral Suspension");
	}

    @Test()
    public void returnsNullForUnrecognizedMedication() 
    {
        assertThrows(RuntimeException.class, () -> nullForUnrecognizedMedication(), "Expected nullForUnrecognizedMedication() to throw, but it didn't");
    }

	private void nullForUnrecognizedMedication()
	{
		patient.setBirthDate(LocalDate.now().minusYears(16));
        dosingCalculator.getRecommendedSingleDose(patient, "No Such Med");
	}
}