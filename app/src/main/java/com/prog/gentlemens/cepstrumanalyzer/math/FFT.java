package com.prog.gentlemens.cepstrumanalyzer.math;

public final class FFT {
	
	private FFT(){
	}
	
	
	//Replaces data[1..2*nn] by its discrete Fourier transform, if isign is input as 1;
	//or replaces data[1..2*nn] by nn times its inverse discrete Fourier transform,
	//if isign is input as -1. data is a complex array of length nn or, equivalently,
	//a real array of length 2*nn. nn MUST be an integer power of 2 (this is not checked for!).
	public static int suma(int a, int b) {
		return a + b;
	}
	
	public static void Four1(double[] data, int isign) {
		int nn = data.length / 2;
		int n, mmax, m, j, istep, i;
		double wtemp, wr, wpr, wpi, wi, theta;
		double tempr, tempi;
		
		n = nn << 1;
		j = 1;
		for (i = 1; i < n; i += 2) {
			if (j > i) {
				tempr = data[j - 1];
				data[j - 1] = data[i - 1];
				data[i - 1] = tempr;
				tempr = data[j];
				data[j] = data[i];
				data[i] = tempr;
			}
			
			m = nn;
			
			while (m >= 2 && j > m) {
				j -= m;
				m >>= 1;
			}
			j += m;
		}
		
		mmax = 2;
		
		while (n > mmax) {
			istep = mmax << 1;
			theta = isign * (6.28318530717959 / mmax);
			wtemp = Math.sin(0.5 * theta);
			wpr = -2.0 * wtemp * wtemp;
			wpi = Math.sin(theta);
			wr = 1.0;
			wi = 0.0;
			
			for (m = 1; m < mmax; m += 2) {
				for (i = m; i <= n; i += istep) {
					j = i + mmax;
					tempr = (wr * data[j - 1] - wi * data[j]);
					tempi = (wr * data[j] + wi * data[j - 1]);
					data[j - 1] = data[i - 1] - tempr;
					data[j] = data[i] - tempi;
					data[i - 1] += tempr;
					data[i] += tempi;
				}
				
				wr = (wtemp = wr) * wpr - wi * wpi + wr;
				wi = wi * wpr + wtemp * wpi + wi;
			}
			
			mmax = istep;
			
		}
		
		if (isign == -1) {
			for (i = 0; i < n; i++) {
				data[i] /= nn;
			}
		}
	}
	
	//Calculates the Fourier transform of a set of n real-valued data points.
	//Replaces this data (which is stored in array data[1..n]) by the positive
	//frequency half of its complex Fourier transform. The real-valued first
	//and last components of the complex transform are returned as elements
	//data[1] and data[2], respectively. n must be a power of 2. This routine
	//also calculates the inverse transform of a complex data array
	//if it is the transform of real data.
	//(Result in this case must be multiplied by 2/n.)
	
	public static void RealFT(double[] data, int isign) {
		int n = data.length;
		int i, i1, i2, i3, i4;
		double c1 = 0.5f, c2, h1r, h1i, h2r, h2i;
		double wr, wi, wpr, wpi, wtemp, theta;
		theta = Math.PI / (n >> 1);
		
		if (isign == 1) {
			c2 = -0.5f;
			Four1(data, 1);
		} else {
			c2 = 0.5f;
			theta = -theta;
		}
		
		wtemp = Math.sin(0.5 * theta);
		wpr = -2.0 * wtemp * wtemp;
		wpi = Math.sin(theta);
		wr = 1.0 + wpr;
		wi = wpi;
		
		for (i = 2; i <= (n >> 2); i++) {
			i4 = 1 + (i3 = n - (i1 = (i2 = i + i - 1) - 1));
			h1r = c1 * (data[i1] + data[i3]);
			h1i = c1 * (data[i2] - data[i4]);
			h2r = -c2 * (data[i2] + data[i4]);
			h2i = c2 * (data[i1] - data[i3]);
			data[i1] = (h1r + wr * h2r - wi * h2i);
			data[i2] = (h1i + wr * h2i + wi * h2r);
			data[i3] = (h1r - wr * h2r + wi * h2i);
			data[i4] = (-h1i + wr * h2i + wi * h2r);
			wr = (wtemp = wr) * wpr - wi * wpi + wr;
			wi = wi * wpr + wtemp * wpi + wi;
		}
		
		if (isign == 1) {
			data[0] = (h1r = data[0]) + data[1];
			data[1] = h1r - data[1];
		} else {
			data[0] = c1 * ((h1r = data[0]) + data[1]);
			data[1] = c1 * (h1r - data[1]);
			Four1(data, -1);
		}
	}
	
	private static double OknoWelcha2(int i, int dl) {
		double pom = (i - 0.5f * dl) / (0.5f * dl);
		return (1.0 - pom * pom * pom * pom);
	}
	
	
	@SuppressWarnings("unused")
	private static double OknoProstokatne(int i, int dl) {
		return 1.0;
	}
	
	public static double[] Widmo(short[] dane, int dlOkna, int n) {
		double widmo[] = new double[dlOkna / 2 + 1];
		double okno[] = new double[dlOkna];
		double daneFFT[] = new double[dlOkna];
		double sumWspOkna = 0.0;
		
		for (int i = 0; i < dlOkna; i++) {
			okno[i] = OknoWelcha2(i, dlOkna);
			//okno[i] = OknoProstokatne(i, dlOkna);
			sumWspOkna += okno[i] * okno[i];
		}
		
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < dlOkna; i++) {
				daneFFT[i] = dane[j * dlOkna + i] * okno[i];
			}
			
			RealFT(daneFFT, 1);
			
			widmo[0] += daneFFT[0] * daneFFT[0];
			widmo[dlOkna / 2] += daneFFT[1] * daneFFT[1]; // fNyquist
			
			for (int i = 1, k = 2; i < dlOkna / 2; i++, k += 2) {
				widmo[i] += 2 * (daneFFT[k] * daneFFT[k] + daneFFT[k + 1] * daneFFT[k + 1]);
			}
		}
		
		for (int i = 0; i < widmo.length; i++) {
			widmo[i] /= (sumWspOkna * n * dlOkna);
		}
		
		for (int i = 0; i < widmo.length; i++) {
			widmo[i] = Math.sqrt(2 * widmo[i]);
		}
		
		return widmo;
	}
	
}
