package org.geogebra.common.kernel.geos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.Test;

import com.himamis.retex.editor.share.util.Unicode;

public class GeoInputBoxForComplexTest extends BaseUnitTest {

	public static final String IMAGINARY_UNIT = "i";

	@Test
	public void rootOfMinusOneShouldBeImaginaryWithComplexNumber() {
		add("z_1 = 3 + 2i");
		shouldBeUpdatedAs("sqrt(-1)", IMAGINARY_UNIT);
	}

	@Test
	public void rootOfMinusOneShouldBeUsedInExpression() {
		add("z_1 = 1 + 6i");
		shouldBeUpdatedAs("2 + 3sqrt(-1)", "2 + 3" + IMAGINARY_UNIT);
	}

	@Test
	public void sinShouldBeTyped() {
		add("z_1 = 3+2i");
		shouldBeUpdatedAs("sin45", "sin(45)");
	}

	private void shouldBeUpdatedAs(String updatedText, String expected) {
		GeoInputBox inputBox = addAvInput("ib = InputBox(z_1)");
		inputBox.updateLinkedGeo(updatedText);
		assertEquals(expected, inputBox.getText());
	}

	@Test
	public void testImaginaryShouldRenderedAsRegularI() {
		GeoInputBox inputBox = withComplexLinkedGeo();
		assertEquals("3 + 2 \\; i", inputBox.getText());
	}

	protected GeoInputBox withLinkedGeo(String definition, String label, String value) {
		add(definition + " = " + value);
		GeoInputBox inputBox = add("InputBox(" + label + ")");
		inputBox.setSymbolicMode(true);
		return inputBox;
	}

	protected GeoInputBox withLinkedGeo(String label, String value) {
		return withLinkedGeo(label, label, value);
	}

	protected GeoInputBox withComplexLinkedGeo() {
		return withLinkedGeo("z_1", "3+2i");
	}

	@Test
	public void testImaginaryShouldEditedAsRegularI() {
		GeoInputBox inputBox = withComplexLinkedGeo();
		assertEquals("3+2 i", inputBox.getTextForEditor());
	}

	@Test
	public void testOnUpdateImaginaryShouldBeUsed() {
		GeoInputBox inputBox = withComplexLinkedGeo();
		inputBox.updateLinkedGeo("4 + 5" + Unicode.IMAGINARY);
		assertEquals("4 + 5 \\; i", inputBox.getText());
		assertEquals("4+5 i", inputBox.getTextForEditor());
	}

	@Test
	public void capitalIShouldBeSmallIWhenComplex() {
		GeoInputBox inputBox = withComplexLinkedGeo();
		inputBox.updateLinkedGeo("4+5I");
		assertEquals("4 + 5 \\; i", inputBox.getText());
	}

	@Test
	public void formulaTextShouldUseRegularIWhenComplex() {
		GeoInputBox inputBox = withComplexLinkedGeo();
		GeoText text = add("FormulaText[InputBox1]");
		assertEquals("3 + 2 \\; i", text.getTextString());
	}

	@Test
	public void inputBoxPlusStringShouldUseImaginaryWhenComplex() {
		GeoInputBox inputBox = withComplexLinkedGeo();
		GeoText text = add("InputBox1 + \"\"");
		assertEquals("3 + 2" + Unicode.IMAGINARY, text.getTextString());
	}

	@Test
	public void formulaTextOnePlusIShouldUseRegularI() {
		GeoText text = add("FormulaText(1+" + Unicode.IMAGINARY + ")");
		assertEquals("1 + i", text.getTextString());
	}

	@Test
	public void addOnePlusIShouldUseImaginary() {
		GeoText text = add("(1 + " + Unicode.IMAGINARY + ") + \"\"");
		assertEquals("(1 + " + Unicode.IMAGINARY + ")", text.getTextString());
	}

	@Test
	public void textOnePlusIShouldUseImaginary() {
		GeoText text = add("Text(1+" + Unicode.IMAGINARY + ")");
		assertEquals("1 + " + Unicode.IMAGINARY, text.getTextString());
	}

	@Test
	public void functionVariableEShouldStayAsE() {
		GeoInputBox inputBox = withLinkedGeo("g(e)", "g", "?");
		GeoNumeric a = add("a = g(1)");
		inputBox.updateLinkedGeo("e");
		assertEquals(1, a.getValue(), 0);
	}

	@Test
	public void functionVariableIShouldStayAsI() {
		GeoInputBox inputBox = withLinkedGeo("g(i)", "g", "?");
		GeoNumeric a = add("a = g(1)");
		inputBox.updateLinkedGeo("3i/2");
		assertEquals(1.5, a.getValue(), 0);
	}
}
