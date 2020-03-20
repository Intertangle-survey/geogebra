package org.geogebra.common.kernel.arithmetic.vector;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.printing.printable.vector.PrintableVector;
import org.geogebra.common.kernel.printing.printer.expression.ExpressionPrinter;
import org.geogebra.common.kernel.printing.printer.Printer;

class VectorPrinter implements Printer {

    private PrintableVector vector;

    VectorPrinter(PrintableVector vector) {
        this.vector = vector;
    }

    @Override
    public String print(StringTemplate tpl, ExpressionPrinter expressionPrinter) {
        return printLeftParenthesis(tpl)
                + expressionPrinter.print(vector.getX(), tpl)
                + printDelimiter(tpl)
                + expressionPrinter.print(vector.getY(), tpl)
                + printRightParenthesis(tpl);
    }

    private String printLeftParenthesis(StringTemplate tpl) {
        return tpl == StringTemplate.editorTemplate ? "{{" : tpl.leftBracket();
    }

    private String printRightParenthesis(StringTemplate tpl) {
        return tpl == StringTemplate.editorTemplate ? "}}" : tpl.rightBracket();
    }

    private String printDelimiter(StringTemplate tpl) {
        if (tpl == StringTemplate.editorTemplate) {
            return "}, {";
        } else if (vector.getCoordinateSystem() == Kernel.COORD_CARTESIAN) {
            return ", ";
        } else {
            return "; ";
        }
    }
}