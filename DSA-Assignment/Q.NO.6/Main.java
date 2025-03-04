class NumberPrinter {
    public void printZero() {
        System.out.print(0);
    }
    
    public void printEven(int number) {
        System.out.print(number);
    }
    
    public void printOdd(int number) {
        System.out.print(number);
    }
}

class ThreadController {
    private int n;
    private int current = 0;
    private final Object lock = new Object();
    
    public ThreadController(int n) {
        this.n = n;
    }
    
    public void zero(NumberPrinter printer) {
        synchronized (lock) {
            for (int i = 1; i <= n; i++) {
                while (current % 2 != 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printer.printZero();
                current++;
                lock.notifyAll();
            }
        }
    }
    
    public void even(NumberPrinter printer) {
        synchronized (lock) {
            for (int i = 2; i <= n; i += 2) {
                while (current % 2 == 0 || i % 2 != 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printer.printEven(i);
                current++;
                lock.notifyAll();
            }
        }
    }
    
    public void odd(NumberPrinter printer) {
        synchronized (lock) {
            for (int i = 1; i <= n; i += 2) {
                while (current % 2 == 0 || i % 2 == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printer.printOdd(i);
                current++;
                lock.notifyAll();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        int n = 5;
        NumberPrinter printer = new NumberPrinter();
        ThreadController controller = new ThreadController(n);
        
        Thread zeroThread = new Thread(() -> controller.zero(printer));
        Thread evenThread = new Thread(() -> controller.even(printer));
        Thread oddThread = new Thread(() -> controller.odd(printer));
        
        zeroThread.start();
        evenThread.start();
        oddThread.start();
    }
}
