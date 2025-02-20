package sorts.hybrid;

import sorts.templates.Sort;
import sorts.insert.BinaryInsertionSort;
import utils.IndexedRotations;
import main.ArrayVisualizer;

/*
 * 
MIT License

Copyright (c) 2021-2024 aphitorite

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 *
 */

final public class InPlaceStableCycleSort extends Sort {
	public InPlaceStableCycleSort(ArrayVisualizer arrayVisualizer) {
		super(arrayVisualizer);
		
		this.setSortListName("In-Place Stable Cycle (IPSC)");
		this.setRunAllSortsName("In-Place Stable Cycle Sort");
		this.setRunSortName("In-Place Stable Cyclesort");
		this.setCategory("Hybrid Sorts");
		this.setComparisonBased(true);
		this.setBucketSort(false);
		this.setRadixSort(false);
		this.setUnreasonablySlow(false);
		this.setUnreasonableLimit(0);
		this.setBogoSort(false);
	}
	
	//stable sorting algorithm performing a worst case of 
	//O(n^2) comparisons and O(n) moves in O(1) space
	
	private void multiSwap(int[] array, int a, int b, int len) {
		for(int i = 0; i < len; i++)
			Writes.swap(array, a+i, b+i, 1, true, false);
	}
	
	private void rotate(int[] array, int a, int m, int b) {
		Highlights.clearAllMarks();
		IndexedRotations.cycleReverse(array, a, m, b, 1, true, false);
	}
	
	private long getRank(int[] array, int a, int b, int r) {
		int c = 0, ce = 0;
		
		for(int i = a; i < b; i++) {
			if(i == r) continue;
			
			Highlights.markArray(2, i);
			Delays.sleep(0.01);
			
			int cmp = Reads.compareValues(array[i], array[r]);
			
			c  += cmp == -1 ? 1 : 0;
			ce += cmp <=  0 ? 1 : 0;
		}
		Highlights.clearMark(2);
		
		return (long)ce << 32 | (long)c;
	}
	
	private int selectMedian(int[] array, int a, int b) {
		int med = (b-a)/2;
		int min = a, max = min;
		
		for(int i = a+1; i < b; i++) {
			if(Reads.compareIndices(array, i, min, 0.25, true) < 0)
				min = i;
			else if(Reads.compareIndices(array, i, max, 0.25, true) > 0)
				max = i;
		}
		//max or min might be the median
		long rank = this.getRank(array, a, b, min);
		int r = (int)rank, re = (int)(rank >> 32);
		
		if(med >= r && med <= re) return array[min];
		
		rank = this.getRank(array, a, b, max);
		r = (int)rank; re = (int)(rank >> 32);
		
		if(med >= r && med <= re) return array[max];
		
		for(int i = a;; i++) {
			Highlights.markArray(1, i);
			Delays.sleep(0.5);
			
			if(Reads.compareValues(array[i], array[min]) > 0
			   && Reads.compareValues(array[i], array[max]) < 0) {
				   
				rank = this.getRank(array, a, b, i);
				r = (int)rank; re = (int)(rank >> 32);
				
				if(med >= r && med <= re)
					return array[i];
				
				else if(re < med)
					min = i;
				
				else max = i;
			}
		}
	}
	
	////////////////////
	//                //
	//  PARTITIONING  //
	//                //
	////////////////////
	
	private void resetBits(int[] array, int pa, int pb, int bLen) {
		pa--; pb--;
		for(int i = 0; i < bLen; i++)
			if(Reads.compareIndices(array, ++pa, ++pb, 0.5, true) > 0)
				Writes.swap(array, pa, pb, 0.5, true, false);
	}
	
	private boolean initBitBuffer(int[] array, int a, int b, int piv, int bLen) {
		int p = b, aCnt = 0, bCnt = 0, tCnt = 0;
		
		//find lesser and greater elements
		for(int i = b; i > a && tCnt < 2*bLen; i--) {
			int pCmp = Reads.compareIndexValue(array, i-1, piv, 1, true);
			
			if(aCnt < bLen && pCmp < 0) {
				this.rotate(array, i, p-tCnt, p);
				p = i+tCnt++;
				aCnt++;
			}
			else if(bCnt < bLen && pCmp > 0) {
				this.rotate(array, i, p-tCnt, p);
				p = i+tCnt++;
				this.rotate(array, i-1, i, p-bCnt);
				bCnt++;
			}
		}
		this.rotate(array, p-tCnt, p, b);
		if(tCnt == 2*bLen) return false;
		
		//if not enough were found either terminate or search equal elements
		
		int b1 = b-tCnt;
		
		//redistribute
		if(aCnt < bLen && bCnt < bLen) {
			BinaryInsertionSort smallSort = new BinaryInsertionSort(this.arrayVisualizer);
			smallSort.customBinaryInsert(array, b1, b, 0.125);
			this.rotate(array, a, b1, b-bCnt);
			return true;
		}
		
		//search equals
		
		int eCnt = 0, eLen = tCnt-bLen;
		p = b1;
		
		for(int i = b1; eCnt < eLen; i--) {
			if(Reads.compareIndexValue(array, i-1, piv, 1, true) == 0) {
				this.rotate(array, i, p-eCnt, p);
				p = i+eCnt++;
				eCnt++;
			}
		}
		this.rotate(array, p-eLen, p, b1);
		this.rotate(array, b-2*bLen, b1, b-bCnt);
		
		return false;
	}
	
	//@param cmp - comp val that means bit flipped
	private int blockCyclePartitionDest(int[] array, int a, int a1, int b1, int b, int pa, int pb, int piv, int bLen, int cmp) {
		int d = a1, e = 0;
		int pCmp = Reads.compareValues(array[a1], piv);
		
		for(int i = a1+bLen; i < b; i += bLen) {
			Highlights.markArray(2, i);
			int vCmp = Reads.compareValues(array[i], piv);
			
			if(vCmp < pCmp) d += bLen;
			else if(i < b1 && Reads.compareIndices(array, pa+(i-a)/bLen, pb+(i-a)/bLen, 0.01, true) != cmp 
						   && vCmp == pCmp) e++;
			
			Highlights.markArray(3, d);
			Delays.sleep(0.01);
		}
		while(Reads.compareIndices(array, pa+(d-a)/bLen, pb+(d-a)/bLen, 0.01, true) == cmp || e-- > 0) {
			d += bLen;
			
			Highlights.markArray(3, d);
			Delays.sleep(0.01);
		}
		
		return d;
	}
	//b-a is divisible by bLen
	private void blockCyclePartition(int[] array, int a, int b, int pa, int pb, int piv, int bLen, int cmp) {
		for(int i = a; i < b; i += bLen) {
			if(Reads.compareIndices(array, pa+(i-a)/bLen, pb+(i-a)/bLen, 1, true) != cmp) {
				Highlights.markArray(1, i);
				int j = i;
				
				for(;;) {
					int k = this.blockCyclePartitionDest(array, a, i, j, b, pa, pb, piv, bLen, cmp);
					
					Writes.swap(array, pa+(k-a)/bLen, pb+(k-a)/bLen, 0.02, true, false);
					if(k == i) break;
					this.multiSwap(array, i, k, bLen);
					
					j = k;
				}
			}
		}
	}
	
	private int leftBinSearch(int[] array, int a, int b, int val) {
		while(a < b) {
			int m = a+(b-a)/2;
			
			if(Reads.compareValues(val, array[m]) <= 0) 
				b = m;
			else	 
				a = m+1;
		}
		return a;
	}
	private int rightBinSearch(int[] array, int a, int b, int val) {
		while(a < b) {
			int m = a+(b-a)/2;
			
			if(Reads.compareValues(val, array[m]) < 0) 
				b = m;
			else	 
				a = m+1;
		}
		return a;
	}
	private void merge(int[] array, int[] cnt, int a, int m, int b, int piv) {
		int m1 = this.leftBinSearch(array, m, b, piv);
		int m2 = this.rightBinSearch(array, m1, b, piv);
		
		int aCnt = m1-m, mCnt = m2-m1, bCnt = b-m2;
		
		this.rotate(array, a+cnt[0], m, m1);
		cnt[0] += aCnt;
		
		this.rotate(array, a+cnt[0]+cnt[1], m1, m2);
		cnt[1] += mCnt;
		cnt[2] += bCnt;
	}
	//optimized for: [	 A	 ][ B ]
	private void mergeEasy(int[] array, int a, int m, int b, int piv) {
		b = this.rightBinSearch(array, m, b, piv);
		int m1 = this.leftBinSearch(array, a, m, piv);
		int m2 = this.rightBinSearch(array, m1, m, piv);
		
		this.rotate(array, m2, m, b);
		
		b = this.leftBinSearch(array, m2, b-(m-m2), piv);
		this.rotate(array, m1, m2, b);
	}
	
	//returns true if u dont have to sort
	private boolean partition(int[] array, int a, int b, int piv) {
		//create bit buffer
		
		int n = b-a, bLen = (int)Math.sqrt(n-1)+1;
		if(this.initBitBuffer(array, a, b, piv, bLen)) return true;
		
		int b1 = b-2*bLen, pa = b1, pb = b1+bLen;
		int cmp = 1;
		
		//partition blocks
		
		for(int i = a; i < b1; i += bLen, cmp = -cmp)
			this.blockCyclePartition(array, i, Math.min(i+bLen, b1), pa, pb, piv, 1, cmp);
		this.resetBits(array, pa, pb, bLen);
		
		//type blocks
		
		int p = a;
		int[] cnt = {0, 0, 0};
		
		for(int i = a; i < b1; i += bLen) {
			this.merge(array, cnt, p, i, Math.min(i+bLen, b1), piv);
			
			while(cnt[0] >= bLen) {
				cnt[0] -= bLen;
				p += bLen;
			}
			while(cnt[1] >= bLen) {
				this.rotate(array, p, p+cnt[0], p+cnt[0]+bLen);
				cnt[1] -= bLen;
				p += bLen;
			}
			while(cnt[2] >= bLen) {
				this.rotate(array, p, p+cnt[0]+cnt[1], p+cnt[0]+cnt[1]+bLen);
				cnt[2] -= bLen;
				p += bLen;
			}
		}
		this.blockCyclePartition(array, a, p, pa, pb, piv, bLen, 1);
		this.resetBits(array, pa, pb, bLen);
		
		//rotate lesser and eq items + redistribute bits
		
		this.mergeEasy(array, p, b1, b, piv);
		this.mergeEasy(array, a, p, b, piv);
		
		return false;
	}
	
	/////////////////////////
	//                     //
	//  STABLE CYCLE SORT  //
	//                     //
	/////////////////////////
	
	//@param cmp - comp val that means bit flipped
	//items equal to the median automatically count as bit flipped
	private int stableCycleDest(int[] array, int a, int a1, int b1, int b, int p, int piv, int cmp) {
		int d = a1, e = 0;
		
		for(int i = a1+1; i < b; i++) {
			Highlights.markArray(2, i);
			
			int pCmp = Reads.compareValues(array[i], piv);
			boolean bit = pCmp == cmp || pCmp == 0;
			
			int val  = bit ? array[p+i-a] : array[i];
			int vCmp = Reads.compareValues(val, array[a1]);
			
			if(vCmp == -1) d++;
			else if(i < b1 && !bit && vCmp == 0) e++;
			
			Highlights.markArray(3, d);
			Delays.sleep(0.01);
		}
		for(;;) {
			int pCmp = Reads.compareValues(array[d], piv);
			boolean bit = pCmp == cmp || pCmp == 0;

			if(!bit && e-- == 0) break;
			d++;
			
			Highlights.markArray(3, d);
			Delays.sleep(0.01);
		}
		
		return d;
	}
	private void stableCycle(int[] array, int a, int b, int p, int piv, int cmp) {
		for(int i = a; i < b; i++) {
			int pCmp = Reads.compareValues(array[i], piv);
			boolean bit = pCmp == cmp || pCmp == 0;
			
			if(!bit) {
				Highlights.markArray(1, i);
				int j = i;
				
				for(;;) {
					int k = this.stableCycleDest(array, a, i, j, b, p, piv, cmp);
					
					if(k == i) break;
					
					int t = array[i];
					Writes.write(array, i, array[k], 0.01, true, false);
					Writes.write(array, k, array[p+k-a], 0.01, true, false);
					Writes.write(array, p+k-a, t, 0.01, true, false);
					
					j = k;
				}
				Writes.swap(array, i, p+i-a, 0.02, true, false);
			}
		}
	}
	
	@Override
	public void runSort(int[] array, int length, int bucketCount) {
		int a = 0, b = length;
		
		if(length <= 32) {
			BinaryInsertionSort smallSort = new BinaryInsertionSort(this.arrayVisualizer);
			smallSort.customBinaryInsert(array, a, b, 0.25);
			return;
		}
		
		int piv = this.selectMedian(array, a, b);
		if(this.partition(array, a, b, piv)) return;
		
		int m2 = this.rightBinSearch(array, a, b, piv);
		int m1 = this.leftBinSearch(array, a, m2, piv);
		
		int h1 = m1-a, h2 = b-m2, hMax = Math.max(h1,h2);
		int a1 = a+hMax, b1 = b-hMax;
		
		this.stableCycle(array, a, a+h1, b1, piv, 1);
		this.multiSwap(array, a+h1, b1+h1, hMax-h1);
		
		this.multiSwap(array, a, b1, hMax-h2);
		this.stableCycle(array, a1-h2, a1, b-h2, piv, -1);
	}
}