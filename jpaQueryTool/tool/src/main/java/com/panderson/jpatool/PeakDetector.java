package com.panderson.jpatool;

/*  w  ww.  j  a  va 2 s .c om*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * http://sites.google.com/site/girishpalshikar/Home/mypublications/
 * SimpleAlgorithmsforPeakDetectioninTimeSeriesACADABAI_2009.pdf
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com> May 10, 2011
 */
public class PeakDetector {

	private static final double offset = 0.1f;

	/**
	 * Return the peak locations as array index for the time series set at creation.
	 * @param windowSize the window size to look for peaks. a neighborhood of +/- windowSize will be inspected to search for peaks. Typical values start at 3.
	 * @param stringency threshold for peak values. Peak with values lower than <code>
	 *             mean + stringency * std</code> will be rejected. <code>Mean</code> and <code>std</code> are calculated on the spikiness function. Typical values range from 1 to 3.
	 * @return an int array, with one element by retained peak, containing the index of the peak in the time series array.
	 */
	public static Integer[] process(double[] T, final int windowSize, final double stringency) {
		// Compute peak function values
		double[] S = new double[T.length];
		double maxLeft = 0, maxRight = 0;
		for (int i = windowSize; i < S.length - windowSize; i++) {
			maxLeft = Math.abs(T[i] - T[i - 1]) > offset ? T[i] - T[i - 1] : 0;
			maxRight = Math.abs(T[i] - T[i + 1]) > offset ? T[i] - T[i + 1] : 0;
			for (int j = 2; j <= windowSize; j++) {
				double a = Math.abs(T[i] - T[i - j]) > offset ? T[i] - T[i - j] : 0;
				double b = Math.abs(T[i] - T[i + j]) > offset ? T[i] - T[i + j] : 0;
				if (a > maxLeft) {
					maxLeft = a;
				}
				if (b > maxRight) {
					maxRight = b;
				}
			}
			S[i] = 0.5f * (maxRight + maxLeft);
		}

		// Compute mean and std of peak function
		double mean = 0;
		int n = 0;
		double M2 = 0;
		double delta;
		for (int i = 0; i < S.length; i++) {
			n = n + 1;
			delta = S[i] - mean;
			mean = mean + delta / n;
			M2 = M2 + delta * (S[i] - mean);
		}

		double variance = M2 / (n - 1);
		double std = (double) Math.sqrt(variance);

		// Collect only large peaks
		List<Integer> peakLocations = new ArrayList<Integer>();
		for (int i = 0; i < S.length; i++) {
			if (S[i] > 0 && (S[i] - mean) > stringency * std) {
				peakLocations.add(i);
			}
		}
		System.out.println("maxLeft: " + maxLeft + ", maxRight: " + maxRight + ", mean: " + mean + ", variance: " + variance + ", std: " + std);

		// Remove peaks too close
		List<Integer> toPrune = new ArrayList<Integer>();
		int peak1, peak2, weakerPeak;
		for (int i = 0; i < peakLocations.size() - 1; i++) {
			peak1 = peakLocations.get(i);
			peak2 = peakLocations.get(i + 1);

			if (peak2 - peak1 < windowSize) {
				// Too close, prune the smallest one
				if (T[peak2] > T[peak1]) {
					weakerPeak = peak1;
				} else {
					weakerPeak = peak2;
				}
				toPrune.add(weakerPeak);
			}
		}
		peakLocations.removeAll(toPrune);

		// Convert to int[]
		Integer[] peakArray = new Integer[peakLocations.size()];
		for (int i = 0; i < peakArray.length; i++) {
			peakArray[i] = peakLocations.get(i);
		}
		return peakArray;

	}  // End of process()

	public static void mxain(String[] args){
		double[] d = {0.000000, 7174.986671, 37.114702, 12.742796, 6.220391, 4.863626, 
  5.504823, 3.215424, 4.590125, 7.801789, 6.025176, 4.594296, 
  4.390227, 4.533470, 5.967590, 5.303840, 3.307375, 5.214141, 
  5.976403, 4.982480, 5.047404, 4.519965, 3.245746, 3.721091, 
  4.642440, 3.637923, 5.687730, 5.638766, 4.199602, 4.525002, 
  3.167828, 3.678495, 3.723812, 4.953418, 12.410477, 20.060791, 
  7.436260, 3.449682, 4.439600, 5.709052, 5.249555, 4.485792, 
  4.965435, 2.818415, 5.719105, 5.526588, 5.076986, 6.149462, 
  5.178730, 2.195552, 4.326901, 3.359316, 6.751365, 3.885870, 
  4.842173, 5.341081, 4.591439, 3.665811, 2.849366, 3.378881, 
  4.919598, 5.262288, 3.876517, 3.794830};

		Integer[] res = process(d, 3, offset);
	  List<Integer> ll = Arrays.asList(res);
		System.out.println(ll);
		
		int ii = 0;
		
		for (Double i : d){
			System.out.print(i);
			if (ll.contains(ii)){
				System.out.println(" <---");
			} else {
				System.out.println("");
			}
			ii++;
		}
	}
}
