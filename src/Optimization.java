import javax.swing.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Optimization {
    public static AtomicInteger count = new AtomicInteger(0);
    static final int NCPUS = Runtime.getRuntime().availableProcessors();

    /** Rungs start with two threads, increasing by two through max */
    static final int DEFAULT_MAX_THREADS = Math.max(4, NCPUS + NCPUS/2);

    /** The Number of replication runs per thread value */
    static final int DEFAULT_REPLICATIONS = 3;

    /** If True, print statistics in SNAPSHOT_RATE intervals */
    static boolean verbose = true;
    static final long SNAPSHOT_RATE = 10000; //miliseconds

    /**
     * The problem size. Each machine is an element of a Factory. Goal is to place machines
     * next to other machines based a similarity matrix
     */
    static final int DEFAULT_STATIONS = 48;

    static final int DEFAULT_FACTORY_SIZE = 48;

    //Tuning Parameters

    /**
     * The number of chromosomes per subpop. Must be a power of two.
     *
     * Smaller values lead to faster iterations but poorer quality
     * results
     */
    static final int DEFAULT_SUBPOP_SIZE = 32;


    /**
     * The number of iterations per subpop. Convergence appears
     * to be roughly proportional to #cities-squared
     */
    static final int DEFAULT_GENERATIONS = DEFAULT_STATIONS * DEFAULT_STATIONS;

    /**
     * The number of subpops. The total population is #subpops * subpopSize,
     * which should be roughly on the order of #cities-squared
     *
     * Smaller values lead to faster total runs but poorer quality
     * results
     */
    static final int DEFAULT_NSUBPOPS = DEFAULT_GENERATIONS/DEFAULT_SUBPOP_SIZE;

    /**
     * The minimum length for a random chromosome strand.
     * Must be at least 1.
     */
    static final int MIN_STRAND_LENGTH = 3;


    /**
     * The probability mask value for creating random strands,
     * that have lengths at least MIN_STRAND_LENGTH, and grow
     * with exponential decay 2^(-(1/(RANDOM_STRAND_MASK + 1)))
     * Must be 1 less than a power of two.
     */
    static final int RANDOM_STRAND_MASK = 7;

    /**
     * Probability control for selecting breeders.
     * Breeders are selected starting at the best-fitness chromosome,
     * with exponentially decaying probability
     * 1 / (subpopSize >>> BREEDER_DECAY).
     *
     * Larger values usually cause faster convergence but poorer
     * quality results
     */
    public static final int BREEDER_DECAY = 1;

    /**
     * Probability control for selecting dyers.
     * Dyers are selected starting at the worst-fitness chromosome,
     * with exponentially decaying probability
     * 1 / (subpopSize >>> DYER_DECAY)
     *
     * Larger values usually cause faster convergence but poorer
     * quality results
     */
    static final int DYER_DECAY = 1;

    public static void main(String[] args) throws InterruptedException {
        int maxThreads = DEFAULT_MAX_THREADS;
        int nStations = DEFAULT_STATIONS;
        int factorSize = DEFAULT_FACTORY_SIZE;
        int subpopSize = DEFAULT_SUBPOP_SIZE;
        int nGen = nStations * nStations;
        int nSubpops = nStations * nStations / subpopSize;
        int nReps = DEFAULT_REPLICATIONS;

        try{
            int argc= 0;
            while (argc < args.length){
                String option = args[argc++];
                if (option.equals("-c")) {
                    nStations = Integer.parseInt(args[argc]);
                    nGen = nStations * nStations;
                    nSubpops = nStations * nStations / subpopSize;
                } else if (option.equals("-s")) {
                    factorSize = Integer.parseInt(args[argc]);
                } else if (option.equals("-p"))
                    subpopSize = Integer.parseInt(args[argc]);
                else if (option.equals("-g"))
                    nGen = Integer.parseInt(args[argc]);
                else if (option.equals("-n"))
                    nSubpops = Integer.parseInt(args[argc]);
                else if (option.equals("-q")) {
                    verbose = false;
                    argc--;
                }
                else if (option.equals("-r"))
                    nReps = Integer.parseInt(args[argc]);
                else
                    maxThreads = Integer.parseInt(option);
                argc++;
            }
        } catch (Exception e){
            reportUsageErrorAndDie();
        }
        System.out.print("TSPExchangerTest");
        System.out.println(" -s " + factorSize);
        System.out.print(" -c " + nStations);
        System.out.print(" -g " + nGen);
        System.out.print(" -p " + subpopSize);
        System.out.print(" -n " + nSubpops);
        System.out.print(" -r " + nReps);
        System.out.print(" max threads " + maxThreads);
        System.out.println();


        if(false && NCPUS>4){
            int h = NCPUS/2;
            System.out.printf("Threads: %4d Warmup\n", h);
            Thread.sleep(500);
        }
        int maxt = Math.min(maxThreads, nSubpops);
        for(int j = 0; j < nReps; ++j) {
            for (int i=2; i<=maxt; i+=2) {
                System.out.printf("Threads: %4d Replication: %2d\n", i, j);
                oneRun(i, nSubpops, subpopSize, nGen, factorSize, nStations);
                Thread.sleep(500);
            }
        }
    }

    static void reportUsageErrorAndDie() {
        System.out.print("usage: TSPExchangerTest");
        System.out.print(" [-c #cities]");
        System.out.print(" [-p #subpopSize]");
        System.out.print(" [-g #generations]");
        System.out.print(" [-n #subpops]");
        System.out.print(" [-r #replications]");
        System.out.print(" [-q <quiet>]");
        System.out.print(" #threads]");
        System.out.println();
        System.exit(0);
    }

    /**
     * Performs one run with the given parameters.  Each run completes
     * when there are fewer than 2 active threads.  When there is
     * only one remaining thread, it will have no one to exchange
     * with, so it is terminated (via interrupt).
     */
    static void oneRun(int nThreads, int nSubpops, int subpopSize, int nGen, int factorySize, int nstations) throws InterruptedException {
        Population p = new Population(nThreads, nSubpops, subpopSize, nGen, factorySize, nstations);
        ProgressMonitor mon = null;
        if(verbose){
            p.printSnapshot(0);
            mon = new ProgressMonitor(p);
            mon.start();
        }
        long startTime = System.nanoTime();
        p.start();
        p.awaitDone();
        long stopTime = System.nanoTime();
        if(mon!=null){
            mon.interrupt();
        }
        p.shutdown();
//        Thread.sleep(100);
        long elapsed = stopTime - startTime;
        double secs = (double) elapsed / 1000000000.0;
        p.printSnapshot(secs);
    }

    static final class Population{
        final Worker[] threads;
        final Subpop[] subpops;
        final Exchanger<int[][]> exchanger;
        final CountDownLatch done;
        final int nGen;
        final int subpopSize;
        final int nThreads;
        final int factorySize;
        final int machinesCount;

        Population(int nThreads, int nSubpop, int subpopSize, int nGen, int factorySize, int machinesCount) {
            this.nThreads = nThreads;
            this.nGen = nGen;
            this.subpopSize = subpopSize;
            this.exchanger = new Exchanger<>();
            this.done = new CountDownLatch(nThreads - 1);
            this.factorySize = factorySize; this.machinesCount = machinesCount;
            this.subpops = new Subpop[nSubpop];
            for (int i = 0; i < nSubpop; i++)
                subpops[i] = new Subpop(this);

            this.threads = new Worker[nThreads];
            int maxExchanges = nGen * nSubpop / nThreads;
            for (int i = 0; i < nThreads; ++i) {
                threads[i] = new Worker(this, maxExchanges);
            }
        }

        void start() {
            for(Worker thread : threads)  thread.start();
        }

        /** Stop the tasks */
        void shutdown() {
            for (Worker thread : threads) thread.interrupt();
        }

        void threadDone() {
            done.countDown();
        }
        /** Wait for tasks to complete */
        void awaitDone() throws InterruptedException {
            done.await();
        }

        int totalExchanges() {
            int xs = 0;
            for(Worker worker: threads){
                xs+=worker.exchanges;
            }
            return xs;
        }

        /**
         * Prints statistics, including best and worst fitness scores
         *
         */

        void printSnapshot(double secs) {
            int xs = totalExchanges();
            long rate = (xs == 0) ? 0L : (long) ((secs * 1000000000.0) / xs);
            Chromosome bestc = subpops[0].chromosomes[0];
            Chromosome worstc = bestc;
            for (int k = 0; k < subpops.length; ++k) {
                Chromosome[] cs = subpops[k].chromosomes;
                if (cs[0].fitness > bestc.fitness)
                    bestc = cs[0];
                if (cs[cs.length - 1].fitness < worstc.fitness)
                    worstc = cs[cs.length - 1];
            }
            System.out.printf("N:%4d T:%8.3f B:%6.3f W:%6.3f X:%9d R:%7d\n",
                    nThreads, secs, bestc.fitness, worstc.fitness, xs, rate);
        }

    }

    static final class Worker extends Thread {
        final Population pop;
        final int maxExchanges;
        int exchanges;
        final RNG rng = new RNG();

        Worker(Population pop, int maxExchanges){
            this.pop = pop; this.maxExchanges = maxExchanges;
        }

        /**
         * Repeatedly, find a subpop that is not being updated by another thread, and run a rnadom number of updates on it.
         *
         */
        public void run() {
            try{
                int len = pop.subpops.length;
                int pos = (rng.next() & 0x7FFFFFFF) % len;
                while(exchanges < maxExchanges) {
                    Subpop s = pop.subpops[pos];
                    AtomicBoolean busy = s.busy; //Locking
                    if(!busy.get() && busy.compareAndSet(false, true)){
                        exchanges += s.runUpdates();
                        busy.set(false);
                        pos=(rng.next() & 0x7FFFFFFF) % len; // get the non negative integer
                    }
                    else if (++pos >= len) {
                        pos=0;
                    }
                }
                pop.threadDone();

            } catch (InterruptedException fallThrough) {

            }
        }
    }

    static final class Subpop {
        final Chromosome[] chromosomes;
        final Population pop;
        final AtomicBoolean busy;
        final Exchanger<int[][]> exchanger;
        int[][] exchangeLayout;
        final RNG rng;
        final int subpopSize;
        final int FACTORY_SIZE;
        final int MUTATION_RATE = 5; // 5% chance of mutation

        Subpop(Population pop) {
            this.pop = pop;
            this.subpopSize = pop.subpopSize;
            this.exchanger = pop.exchanger;
            this.busy = new AtomicBoolean(false);
            this.rng = new RNG();
            this.FACTORY_SIZE = pop.factorySize;
            this.chromosomes = new Chromosome[subpopSize];
            for (int j = 0; j < subpopSize; ++j) {
                chromosomes[j] = new Chromosome(FACTORY_SIZE, pop.machinesCount, rng);
            }
            Arrays.sort(chromosomes);
            this.exchangeLayout = new int[FACTORY_SIZE][FACTORY_SIZE];
        }

        int runUpdates() throws InterruptedException {
            int n = 1 + (rng.next() & ((subpopSize << 1) - 1));
            for (int i = 0; i < n; ++i) {
                update();
            }
            return n;
        }

        void update() throws InterruptedException {
            int breederIndex = chooseBreeder();
            int dyerIndex = chooseDyer(breederIndex);
            Chromosome breeder = chromosomes[breederIndex];
            Chromosome child = chromosomes[dyerIndex];

            // Prepare half of the breeder's layout for exchange
            for (int i = 0; i < FACTORY_SIZE; i++) {
                System.arraycopy(breeder.layout[i], 0, exchangeLayout[i], 0, FACTORY_SIZE / 2);
            }

            // Exchange half of the layout with another subpop
            int[][] receivedHalf = exchanger.exchange(exchangeLayout);

            // Perform crossover
            cross(breeder, receivedHalf, child);

            // Fix the order of chromosomes based on the new fitness
            fixOrder(child, dyerIndex);
        }

        int chooseBreeder() {
            int mask = (subpopSize >>> BREEDER_DECAY) - 1;
            int b = 0;
            while ((rng.next() & mask) != mask) {
                if (++b >= subpopSize) b = 0;
            }
            return b;
        }

        int chooseDyer(int exclude) {
            int mask = (subpopSize >>> DYER_DECAY) - 1;
            int d = subpopSize - 1;
            while (d == exclude || (rng.next() & mask) != mask) {
                if (--d < 0) d = subpopSize - 1;
            }
            return d;
        }

        void cross(Chromosome breeder, int[][] receivedHalf, Chromosome child) {
            // Copy the first half from the breeder
            for (int i = 0; i < FACTORY_SIZE; i++) {
                System.arraycopy(breeder.layout[i], 0, child.layout[i], 0, FACTORY_SIZE / 2);
            }

            // Copy the second half from the received layout
            for (int i = 0; i < FACTORY_SIZE; i++) {
                System.arraycopy(receivedHalf[i], FACTORY_SIZE / 2, child.layout[i], FACTORY_SIZE / 2, FACTORY_SIZE / 2);
            }

            // Ensure no duplicate machines and fill empty spaces
            Set<Integer> usedMachines = new HashSet<>();
            List<Integer> availableMachines = new ArrayList<>();
            for (int i = 1; i <= pop.machinesCount; i++) {
                availableMachines.add(i);
            }

            for (int i = 0; i < FACTORY_SIZE; i++) {
                for (int j = 0; j < FACTORY_SIZE; j++) {
                    int machine = child.layout[i][j];
                    if (machine != 0) {
                        if (usedMachines.contains(machine)) {
                            child.layout[i][j] = 0;
                        } else {
                            usedMachines.add(machine);
                            availableMachines.remove(Integer.valueOf(machine));
                        }
                    }
                }
            }

            // Fill empty spaces with remaining machines
            for (int i = 0; i < FACTORY_SIZE && !availableMachines.isEmpty(); i++) {
                for (int j = 0; j < FACTORY_SIZE && !availableMachines.isEmpty(); j++) {
                    if (child.layout[i][j] == 0) {
                        int index = (rng.next() & 0x7FFFFFFF) % availableMachines.size();
                        child.layout[i][j] = availableMachines.remove(index);
                    }
                }
            }

            // Apply mutation
            if ((rng.next() & 0x7FFFFFFF) % 100 < MUTATION_RATE) {
                mutate(child);
            }

            child.calculateFitness();
        }

        void mutate(Chromosome chromosome) {
            // Implement mutation (e.g., swap two random machines)
            int x1 = (rng.next() & 0x7FFFFFFF) % FACTORY_SIZE;
            int y1 = (rng.next() & 0x7FFFFFFF) % FACTORY_SIZE;
            int x2 = (rng.next() & 0x7FFFFFFF) % FACTORY_SIZE;
            int y2 = (rng.next() & 0x7FFFFFFF) % FACTORY_SIZE;

            int temp = chromosome.layout[x1][y1];
            chromosome.layout[x1][y1] = chromosome.layout[x2][y2];
            chromosome.layout[x2][y2] = temp;
        }

        void fixOrder(Chromosome c, int k) {
            Chromosome[] cs = chromosomes;
            double oldFitness = c.fitness;
            c.calculateFitness();
            double newFitness = c.fitness;
            if (newFitness < oldFitness) {
                int j = k;
                int p = j - 1;
                while (p >= 0 && cs[p].fitness > newFitness) {
                    cs[j] = cs[p];
                    j = p--;
                }
                cs[j] = c;
            } else if (newFitness > oldFitness) {
                int j = k;
                int n = j + 1;
                while (n < cs.length && cs[n].fitness < newFitness) {
                    cs[j] = cs[n];
                    j = n++;
                }
                cs[j] = c;
            }
        }
    }

    /**
     * A Chromosome is a Factory Layout candidate
     * One Factory
     */
    static final class Chromosome implements Comparable<Chromosome> {
        int[][] layout;
        final int FACTORY_SIZE;
        final int MACHINES_COUNT;
        final int TYPES_OF_MACHINES = 5;
        double fitness;

        Chromosome(int size, int machinesCount, RNG rng) {
            this.FACTORY_SIZE = size;
            this.MACHINES_COUNT = machinesCount;
            initializeLayout(rng);
            calculateFitness();
        }

        void initializeLayout(RNG rng) {
            layout = new int[FACTORY_SIZE][FACTORY_SIZE];
            int placed = 0;
            while (placed < MACHINES_COUNT) {
                int x = (rng.next() & 0x7FFFFFFF) % FACTORY_SIZE;
                int y = (rng.next() & 0x7FFFFFFF) % FACTORY_SIZE;
                if (layout[x][y] == 0) {
                    layout[x][y] = (rng.next() % TYPES_OF_MACHINES) + 1;
                    placed++;
                }
            }
        }

        void calculateFitness() {
            // Implement fitness calculation based on machine proximity and efficiency
            // This is a placeholder and should be replaced with actual logic
            fitness = 0;
            for (int i = 0; i < FACTORY_SIZE; i++) {
                for (int j = 0; j < FACTORY_SIZE; j++) {
                    if (layout[i][j] != 0) {
                        fitness += calculateMachineScore(i, j);
                    }
                }
            }
        }

        double calculateMachineScore(int x, int y) {
            // Implement logic to calculate score for a single machine
            // based on its proximity to other machines it should be close to
            // This is a placeholder and should be replaced with actual logic
            return 1.0;
        }

        @Override
        public int compareTo(Chromosome other) {
            return Double.compare(other.fitness, this.fitness);
        }
    }


    /**
     * Why not use Concurrent.Random...
     */
    static final class RNG {
        /** Seed generator for XorShift RNGs */
        static final Random seedGenerator = new Random();

        int seed;
        RNG(int seed) { this.seed = seed; }
        RNG()         { this.seed = seedGenerator.nextInt() | 1; }

        int next() {
            int x = seed;
            x ^= x << 6;
            x ^= x >>> 21;
            x ^= x << 7;
            seed = x;
            return x;
        }
    }

    static final class ProgressMonitor extends Thread {
        final Population pop;
        ProgressMonitor(Population p) { pop = p; }
        public void run() {
            double time = 0;
            try {
                while (!Thread.interrupted()) {
                    sleep(SNAPSHOT_RATE);
                    time += SNAPSHOT_RATE;
                    pop.printSnapshot(time / 1000.0);
                }
            } catch (InterruptedException ie) {}
        }
    }

}
