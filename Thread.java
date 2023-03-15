import java.util.Currency;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Thread extends java.lang.Thread implements Runnable {

    public CyclicBarrier barrier;

    int len;            // size of map (len x len) == heatmap dimensions
    double map[][];     // Heatmap
    int thd;            // thread number
    int threadNum;      // thread number
    int midLen;                     // length of map - 2 (for border) == inner grid number of cells per row
    int num_of_cells;               // total number of cells in inner grid
    int row;                        // row of cell
    int col;                        // column of cell

    double[] totalAvg ;
    double[] totalError ;
    int[] iterations ;

    int rowLength;
    int colLength;

    public Thread(double[][] map, int len, int thd, int threadNum, CyclicBarrier barrier, int[] iterations, double[] totalAvg, double[] totalError) {

        this.map = map;                         //  2D heatmap array
        this.len = len;                         // length & width of 2D array
        this.thd = thd;                         // thread number
        this.threadNum = threadNum;             // number of threads
        this.barrier = barrier;                 // barrier for threads
        this.midLen = len - 2;                  // Length of inner grid (i.e., excluding borders)
        this.num_of_cells = midLen * midLen;        // Total # of cells in inner grid
        this.iterations = iterations;
        this.totalAvg = totalAvg;               // Total inner grid average
        this.totalError = totalError;           // Total inner grid average error

        if (threadNum == 4) {
            switch (thd) {
                case 0 -> {
                    this.row = 1;
                    this.col = 1;
                    this.rowLength = midLen / 2;
                    this.colLength = midLen / 2;
                    break;
                }
                case 1 -> {
                    this.row = 1;
                    this.col = midLen / 2 + 1;
                    this.rowLength = (midLen / 2);
                    this.colLength = col + ((midLen / 2) - 1);
                    break;
                }
                case 2 -> {
                    this.row = midLen / 2 + 1;
                    this.col = 1;
                    this.rowLength = row + ((midLen / 2) - 1);
                    this.colLength = (midLen / 2);
                    break;
                }
                case 3 -> {
                    this.row = midLen / 2 + 1;
                    this.col = midLen / 2 + 1;
                    this.rowLength = row + ((midLen / 2) - 1);
                    this.colLength = col + ((midLen / 2) - 1);
                    break;
                }
            }
        } else if (threadNum == 1) {
            //CyclicBarrier bigBarrier= new CyclicBarrier(1);
            this.row = 1;
            this.col = 1;
            this.rowLength = midLen;
            this.colLength = midLen;
        }
    }

    @Override
    public void run() {

        try {
            do {

                System.out.println(Thread.currentThread().getName() + " " + totalError[thd]);

                barrier.await();

                totalError[thd] = 0;
                totalAvg[thd] = 0;

                for (int i = row; i <= rowLength; i++) {
                    for (int j = col; j <= colLength; j++) {

                        double prevAvg = 0;
                        double newAvg = 0;

                        prevAvg = map[i][j];


                        double average = (map[i - 1][j] + map[i + 1][j] + map[i][j - 1] + map[i][j + 1]);

                        map[i][j] = ((average / 4) * 10.0) / 10.0;
                        newAvg = map[i][j];
                        totalAvg[thd] += map[i][j];


                        double error = newAvg - prevAvg;

                        totalError[thd] = totalError[thd] + error;


                    }
                }

                iterations[thd] = iterations[thd] + 1;

                barrier.await();

            } while ( (totalError[0] + totalError[1] + totalError[2] + totalError[3] )  > 5 );                       //totalError[0] > 5) ;         // Continue until total error is less than 5


        } catch (BrokenBarrierException e) {
            barrier.reset();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}