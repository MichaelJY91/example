package example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

/**
 * 题目：写一个计算器类（Calculator），可以实现两个数的加、减、乘、除运算，并可以进行undo和redo操作
 * <p>
 * 使用Calculator结束后必须调用reset()方法，避免计算结果出现误差
 * try{
 *     .....
 * }finally{
 *     Calculator.reset();
 * }
 * </p>
 */
public class Calculator {

    /**
     * 操作符
     */
    private static final char ADD = '+';
    private static final char SUBTRACT = '-';
    private static final char MULTIPLY = '*';
    private static final char DIVIDE = '/';

    /**
     * 操作数栈
     */
    private static ThreadLocal<Stack<Operator>> operatorStack = ThreadLocal.withInitial(Stack::new);

    /**
     * 撤销操作栈
     */
    private static ThreadLocal<Stack<Operator>> undoStack = ThreadLocal.withInitial(Stack::new);

    /**
     * 计算结果
     */
    private static ThreadLocal<BigDecimal> result = ThreadLocal.withInitial(() -> BigDecimal.ZERO);

    /**
     * 加
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal add(BigDecimal n1, BigDecimal n2) {
        return add(n1, n2, false);
    }

    /**
     * 减
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2) {
        return subtract(n1, n2, false);
    }

    /**
     * 乘
     * @param n1
     * @param n2
     * @return
     */
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2) {
        return multiply(n1, n2, false);
    }

    /**
     * 除
     * @param n1
     * @param n2
     * @param scale
     * @param roundingMode
     * @return
     */
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2, Integer scale, RoundingMode roundingMode) {
        return divide(n1, n2, scale, roundingMode, false);
    }

    /**
     * 加
     * @param n1
     * @param n2
     * @param undo 当undo等于false，才将计算操作压操作数栈
     * @return
     */
    private static BigDecimal add(BigDecimal n1, BigDecimal n2, boolean undo) {
        checkArgument(n1, n2);
        result.set(n1.add(n2));
        if (!undo) {
            operatorStack.get().push(Operator.of(ADD, n2));
        }
        return result.get();
    }

    /**
     * 减
     * @param n1
     * @param n2
     * @param undo 当undo等于false，才将计算操作压操作数栈
     * @return
     */
    private static BigDecimal subtract(BigDecimal n1, BigDecimal n2, boolean undo) {
        checkArgument(n1, n2);
        result.set(n1.subtract(n2));
        if (!undo) {
            operatorStack.get().push(Operator.of(SUBTRACT, n2));
        }
        return result.get();
    }

    /**
     * 乘
     * @param n1
     * @param n2
     * @param undo 当undo等于false，才将计算操作压操作数栈
     * @return
     */
    private static BigDecimal multiply(BigDecimal n1, BigDecimal n2, boolean undo) {
        checkArgument(n1, n2);
        result.set(n1.multiply(n2));
        if (!undo) {
            operatorStack.get().push(Operator.of(MULTIPLY, n2));
        }
        return result.get();
    }

    /**
     * 除
     * @param n1
     * @param n2
     * @param scale
     * @param roundingMode
     * @param undo 当undo等于false，才将计算操作压操作数栈
     * @return
     */
    private static BigDecimal divide(BigDecimal n1, BigDecimal n2, Integer scale, RoundingMode roundingMode, boolean undo) {
        checkArgument(n1, n2);
        if (n2.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("illegal argument, n2 must not be zero");
        }

        if (undo) {
            result.set(n1.divide(n2));
        } else {
            if (null == scale || scale <0) {
                throw new IllegalArgumentException("illegal argument, scale must not be empty or less than zero");
            }
            if (null == roundingMode) {
                throw new IllegalArgumentException("illegal argument, roundingMode must not be empty");
            }

            result.set(n1.divide(n2, scale, roundingMode));
            operatorStack.get().push(Operator.of(DIVIDE, n2, scale, roundingMode));
        }
        return result.get();
    }

    /**
     * 撤销
     * @return
     */
    public static BigDecimal undo() {
        Stack<Operator> operators = operatorStack.get();
        if (operators.isEmpty()) {
            return result.get();
        }

        Operator operator = operators.pop();
        BigDecimal tmp;
        switch (operator.op) {
            case ADD:
                tmp = subtract(result.get(), operator.val, true);
                break;
            case SUBTRACT:
                tmp =  add(result.get(), operator.val, true);
                break;
            case MULTIPLY:
                tmp = divide(result.get(), operator.val, null, null, true);
                break;
            case DIVIDE:
                tmp = multiply(result.get(), operator.val, true);
                break;
            default:
                throw new IllegalArgumentException("undo failed, unknown operator");
        }
        undoStack.get().push(operator);
        return tmp;
    }

    /**
     * 重做
     * @return
     */
    public static BigDecimal redo() {
        Stack<Operator> operators = undoStack.get();
        if (operators.isEmpty()) {
            return result.get();
        }

        Operator operator = operators.pop();
        switch (operator.op) {
            case ADD:
                return add(result.get(), operator.val);
            case SUBTRACT:
                return subtract(result.get(), operator.val);
            case MULTIPLY:
                return multiply(result.get(), operator.val);
            case DIVIDE:
                return divide(result.get(), operator.val, operator.scale, operator.roundingMode);
            default:
                throw new IllegalArgumentException("redo failed, unknown operator");
        }
    }

    /**
     * 重置
     * <p>同一个线程使用完后，必须调用reset重置线程共享变量</p>
     */
    public static void reset() {
        operatorStack.remove();
        undoStack.remove();
        result.remove();
    }

    /**
     * 参数检查
     * @param n1
     * @param n2
     */
    private static void checkArgument(BigDecimal n1, BigDecimal n2) {
        if (null == n1 || null == n2) {
            throw new IllegalArgumentException("illegal argument, n1 and n2 must not be empty");
        }
    }

    /**
     * 操作对象
     */
    private static class Operator {
        /**
         * 操作
         */
        private Character op;
        /**
         * 值
         */
        private BigDecimal val;
        /**
         * 小数保留位
         */
        private Integer scale;
        /**
         * 小数超出保留位的截断模式
         */
        private RoundingMode roundingMode;

        private Operator(Character op, BigDecimal val) {
            this.op = op;
            this.val = val;
        }

        private Operator(Character op, BigDecimal val, Integer scale, RoundingMode roundingMode) {
            this(op, val);
            this.scale = scale;
            this.roundingMode = roundingMode;
        }


        public static Operator of(Character op, BigDecimal val) {
            return new Operator(op, val);
        }

        public static Operator of(Character op, BigDecimal val, Integer scale, RoundingMode roundingMode) {
            return new Operator(op, val, scale, roundingMode);
        }
    }

    /**
     * 私有构造
     */
    private Calculator() {}

}
