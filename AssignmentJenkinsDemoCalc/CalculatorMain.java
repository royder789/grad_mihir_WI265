public class CalculatorMain {

    public static void main(String[] args) {

        Calculator calc = new Calculator();

        int result1 = calc.add(11, 5);
        int result2 = calc.subtract(9, 5);
        int result3 = calc.multiply(10, 5);
        int result4 = calc.divide(10, 5);

        System.out.println("Addition: " + result1);
        System.out.println("Subtraction: " + result2);
        System.out.println("Multiplication: " + result3);
        System.out.println("Division: " + result4);
    }
}
