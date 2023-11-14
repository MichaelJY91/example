# 简易计算器
> 支持两个数的加、减、乘、除运算，并可以进行undo和redo操作

## 使用
```
try {
    Calculator.add(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
} finally {
    Calculator.reset();
}

```
## 单侧覆盖
> CalculatorTest