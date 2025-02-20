package sorts.quick;

import java.util.PriorityQueue;

import main.ArrayVisualizer;
import sorts.templates.Sort;

/*

Coded for ArrayV by Ayako-chan
in collaboration with aphitorite, Gaming32 and Scandum

+---------------------------+
| Sorting Algorithm Scarlet |
+---------------------------+

Original name of this algorithm: Adaptive Modified Ternary Stable Priority Quicksort

 */

/**
 * A Median-of-Medians Out-of-Place Stable Quicksort which stores recursions in
 * a priority queue.
 * <p>
 * To use this algorithm in another, use {@code quickSort()} from a reference
 * instance.
 * 
 * @author Ayako-chan - implementation of the sort
 * @author aphitorite - key idea / concept of Priority Quicksort
 * @author Gaming32 - Binary Pattern-Defeating Mergesort
 * @author Scandum - the analyzer before sorting
 *
 */
public final class YanfeiSort extends Sort {

    public YanfeiSort(ArrayVisualizer arrayVisualizer) {
        super(arrayVisualizer);
        this.setSortListName("Yanfei");
        this.setRunAllSortsName("Yanfei Sort");
        this.setRunSortName("Yanfeisort");
        this.setCategory("Quick Sorts");
        this.setComparisonBased(true);
        this.setBucketSort(false);
        this.setRadixSort(false);
        this.setUnreasonablySlow(false);
        this.setUnreasonableLimit(0);
        this.setBogoSort(false);
    }

    class Partition implements Comparable<Partition> {
        public int a, b;
        public boolean bad;

        public Partition(int a, int b, boolean bad) {
            this.a = a;
            this.b = b;
            this.bad = bad;
        }

        public int length() {
            return this.b - this.a;
        }

        @Override
        public int compareTo(Partition y) {
            int len0 = this.length(), len1 = y.length();
            if (len0 < len1) return 1;
            if (len0 > len1) return -1;
            return 0;
        }
    }

    static final int M = 7;

    int threshold = 32;
    int highlight = 0;

    int equ(int a, int b) {
        return ((a - b) >> 31) + ((b - a) >> 31) + 1;
    }

    protected void stableSegmentReversal(int[] array, int start, int end) {
        if (end - start < 3) Writes.swap(array, start, end, 0.75, true, false);
        else Writes.reversal(array, start, end, 0.75, true, false);
        int i = start;
        int left;
        int right;
        while (i < end) {
            left = i;
            while (i < end && Reads.compareIndices(array, i, i + 1, 0.5, true) == 0) i++;
            right = i;
            if (left != right) {
                if (right - left < 3) Writes.swap(array, left, right, 0.75, true, false);
                else Writes.reversal(array, left, right, 0.75, true, false);
            }
            i++;
        }
    }

    protected int medOf3(int[] array, int i0, int i1, int i2) {
        int tmp;
        if(Reads.compareIndices(array, i0, i1, 1, true) > 0) {
            tmp = i1;
            i1 = i0;
        } else tmp = i0;
        if(Reads.compareIndices(array, i1, i2, 1, true) > 0) {
            if(Reads.compareIndices(array, tmp, i2, 1, true) > 0) return tmp;
            return i2;
        }
        return i1;
    }

    public int medP3(int[] array, int a, int b, int d) {
        if (b - a == 3 || (b - a > 3 && d == 0))
            return medOf3(array, a, a + (b - a) / 2, b - 1);
        if (b - a < 3) return a + (b - a) / 2;
        int t = (b - a) / 3;
        int l = medP3(array, a, a + t, --d), c = medP3(array, a + t, b - t, d), r = medP3(array, b - t, b, d);
        // median
        return medOf3(array, l, c, r);
    }

    public int medOfMed(int[] array, int a, int b) {
        int log5 = 0, exp5 = 1, exp5_1 = 0;
        int[] indices = new int[5];
        int n = b - a;
        while (exp5 < n) {
            exp5_1 = exp5;
            log5++;
            exp5 *= 5;
        }
        if (log5 < 1) return a;
        // fill indexes, recursing if required
        if (log5 == 1) for (int i = a, j = 0; i < b; i++, j++) indices[j] = i;
        else {
            n = 0;
            for (int i = a; i < b; i += exp5_1) {
                indices[n] = medOfMed(array, i, Math.min(b, i + exp5_1));
                n++;
            }
        }
        // sort - insertion sort is good enough for 5 elements
        for (int i = 1; i < n; i++) {
            for(int j = i; j > 0; j--) {
                if (Reads.compareIndices(array, indices[j], indices[j - 1], 0.5, true) < 0) {
                    int t = indices[j];
                    indices[j] = indices[j - 1];
                    indices[j - 1] = t;
                } else break;
            }
        }
        // return median
        return indices[(n - 1) / 2];
    }

    protected int binSearch(int[] array, int a, int b, int val, boolean left) {
        while (a < b) {
            int m = a + (b - a) / 2;
            Highlights.markArray(2, highlight + m);
            Delays.sleep(0.25);
            int c = Reads.compareValues(val, array[m]);
            if (c < 0 || (left && c == 0)) b = m;
            else a = m + 1;
        }
        return a;
    }

    protected int leftExpSearch(int[] array, int a, int b, int val, boolean left) {
        int i = 1;
        if (left) while (a - 1 + i < b && Reads.compareValues(val, array[a - 1 + i]) > 0) i *= 2;
        else while (a - 1 + i < b && Reads.compareValues(val, array[a - 1 + i]) >= 0) i *= 2;
        return binSearch(array, a + i / 2, Math.min(b, a - 1 + i), val, left);
    }

    protected int rightExpSearch(int[] array, int a, int b, int val, boolean left) {
        int i = 1;
        if (left) while (b - i >= a && Reads.compareValues(val, array[b - i]) <= 0) i *= 2;
        else while (b - i >= a && Reads.compareValues(val, array[b - i]) < 0) i *= 2;
        return binSearch(array, Math.max(a, b - i + 1), b - i / 2, val, left);
    }

    protected void insertTo(int[] array, int a, int b) {
        Highlights.clearMark(2);
        int temp = array[a];
        int d = (a > b) ? -1 : 1;
        for (int i = a; i != b; i += d)
            Writes.write(array, i, array[i + d], 0.5, true, false);
        if (a != b) Writes.write(array, b, temp, 0.5, true, false);
    }

    protected void mergeFWExt(int[] array, int[] tmp, int a, int m, int b) {
        int len1 = m - a, t = a;
        Highlights.clearMark(2);
        Writes.arraycopy(array, a, tmp, 0, len1, 1, true, true);
        int i = 0, mGallop = M, l = 0, r = 0;
        while (true) {
            do {
                if (Reads.compareValues(tmp[i], array[m]) <= 0) {
                    Writes.write(array, a++, tmp[i++], 1, true, false);
                    l++;
                    r = 0;
                    if (i == len1) return;
                } else {
                    Highlights.markArray(2, m);
                    Writes.write(array, a++, array[m++], 1, true, false);
                    r++;
                    l = 0;
                    if (m == b) {
                        while (i < len1) Writes.write(array, a++, tmp[i++], 1, true, false);
                        return;
                    }
                }
            } while ((l | r) < mGallop);
            do {
                l = leftExpSearch(array, m, b, tmp[i], true) - m;
                for (int j = 0; j < l; j++)
                    Writes.write(array, a++, array[m++], 1, true, false);
                Writes.write(array, a++, tmp[i++], 1, true, false);
                if (i == len1) return;
                if (m == b) {
                    while (i < len1) Writes.write(array, a++, tmp[i++], 1, true, false);
                    return;
                }
                highlight = t;
                r = leftExpSearch(tmp, i, len1, array[m], false) - i;
                highlight = 0;
                for (int j = 0; j < r; j++)
                    Writes.write(array, a++, tmp[i++], 1, true, false);
                Writes.write(array, a++, array[m++], 1, true, false);
                if (i == len1) return;
                if (m == b) {
                    while (i < len1) Writes.write(array, a++, tmp[i++], 1, true, false);
                    return;
                }
                mGallop--;
            } while ((l | r) >= M);
            if (mGallop < 0) mGallop = 0;
            mGallop += 2;
        }
    }

    protected void mergeBWExt(int[] array, int[] tmp, int a, int m, int b) {
        int len2 = b - m, t = a;
        Highlights.clearMark(2);
        Writes.arraycopy(array, m, tmp, 0, len2, 1, true, true);
        int i = len2 - 1, mGallop = M, l = 0, r = 0;
        m--;
        while (true) {
            do {
                if (Reads.compareValues(tmp[i], array[m]) >= 0) {
                    Writes.write(array, --b, tmp[i--], 1, true, false);
                    l++;
                    r = 0;
                    if (i < 0) return;
                } else {
                    Highlights.markArray(2, m);
                    Writes.write(array, --b, array[m--], 1, true, false);
                    r++;
                    l = 0;
                    if (m < a) {
                        while (i >= 0) Writes.write(array, --b, tmp[i--], 1, true, false);
                        return;
                    }
                }
            } while ((l | r) < mGallop);
            do {
                l = (m + 1) - rightExpSearch(array, a, m + 1, tmp[i], false);
                for (int j = 0; j < l; j++)
                    Writes.write(array, --b, array[m--], 1, true, false);
                Writes.write(array, --b, tmp[i--], 1, true, false);
                if (i < 0) return;
                if (m < a) {
                    while (i >= 0) Writes.write(array, --b, tmp[i--], 1, true, false);
                    return;
                }
                highlight = t;
                r = (i + 1) - rightExpSearch(tmp, 0, i + 1, array[m], true);
                highlight = 0;
                for (int j = 0; j < r; j++)
                    Writes.write(array, --b, tmp[i--], 1, true, false);
                Writes.write(array, --b, array[m--], 1, true, false);
                if (i < 0) return;
                if (m < a) {
                    while (i >= 0) Writes.write(array, --b, tmp[i--], 1, true, false);
                    return;
                }
            } while ((l | r) >= M);
            if (mGallop < 0) mGallop = 0;
            mGallop += 2;
        }
    }

    protected void merge(int[] array, int[] buf, int a, int m, int b) {
        if (Reads.compareIndices(array, m - 1, m, 0.0, true) <= 0) return;
        a = leftExpSearch(array, a, m, array[m], false);
        b = rightExpSearch(array, m, b, array[m - 1], true);
        Highlights.clearMark(2);
        if (m - a > b - m) mergeBWExt(array, buf, a, m, b);
        else mergeFWExt(array, buf, a, m, b);
    }

    protected int findRun(int[] array, int a, int b, int mRun) {
        int i = a + 1;
        if (i < b) {
            if (Reads.compareIndices(array, i - 1, i++, 0.5, true) > 0) {
                while (i < b && Reads.compareIndices(array, i - 1, i, 0.5, true) > 0) i++;
                if (i - a < 4) Writes.swap(array, a, i - 1, 1.0, true, false);
                else Writes.reversal(array, a, i - 1, 1.0, true, false);
            } else while (i < b && Reads.compareIndices(array, i - 1, i, 0.5, true) <= 0) i++;
        }
        Highlights.clearMark(2);
        while (i - a < mRun && i < b) {
            insertTo(array, i, rightExpSearch(array, a, i, array[i], false));
            i++;
        }
        return i;
    }

    public void insertSort(int[] array, int a, int b) {
        // technically an insertion sort
        findRun(array, a, b, b - a);
    }

    public void mergeSort(int[] array, int[] buf, int a, int b) {
        int len = b - a;
        if (len <= threshold) {
            insertSort(array, a, b);
            return;
        }
        int mRun = 16;
        int[] runs = Writes.createExternalArray((len - 1) / mRun + 2);
        int r = a, rf = 0;
        while (r < b) {
            Writes.write(runs, rf++, r, 0.5, false, true);
            r = findRun(array, r, b, mRun);
        }
        while (rf > 1) {
            for (int i = 0; i < rf - 1; i += 2) {
                int eIdx;
                if (i + 2 >= rf) eIdx = b;
                else eIdx = runs[i + 2];
                merge(array, buf, runs[i], runs[i + 1], eIdx);
            }
            for (int i = 1, j = 2; i < rf; i++, j+=2, rf--)
                Writes.write(runs, i, runs[j], 0.5, false, true);
        }
        Writes.deleteExternalArray(runs);
    }

    int pivCmp(int v, int piv) {
        int c = Reads.compareValues(v, piv);
        if (c > 0) return 2;
        if (c < 0) return 0;
        return 1;
    }

    protected int[] partition(int[] array, int[] buf, int a, int b, int piv) {
        Highlights.clearMark(2);
        // determines which elements do not need to be moved
        for (; a < b; a++) {
            Highlights.markArray(1, a);
            Delays.sleep(0.25);
            if(Reads.compareValues(array[a], piv) >= 0) break;
        }
        for (; b > a; b--) {
            Highlights.markArray(1, b - 1);
            Delays.sleep(0.25);
            if(Reads.compareValues(array[b - 1], piv) <= 0) break;
        }
        // partitions the list stably
        int[] ptrs = new int[4];
        for (int i = a; i < b; i++) { // count elements (copy to external buffer as well)
            Highlights.markArray(1, i);
            Delays.sleep(0.5);
            Writes.write(buf, i - a, array[i], 0.5, false, true);
            int loc = pivCmp(array[i], piv);
            ptrs[loc]++;
        }
        // prefix sum
        for (int i = 1; i < ptrs.length; i++) ptrs[i] += ptrs[i - 1];
        for (int i = b - a - 1; i >= 0; i--) {// transport elements
            int loc = pivCmp(buf[i], piv);
            Writes.write(array, a + --ptrs[loc], buf[i], 1, true, false);
        }
        for (int i = 0; i < ptrs.length; i++) ptrs[i] += a;
        return new int[] {ptrs[1], ptrs[2]};
    }

    void consumePartition(int[] array, PriorityQueue<Partition> queue, int a, int b, boolean bad) {
        if (b - a > threshold) queue.offer(new Partition(a, b, bad));
        else insertSort(array, a, b);
    }

    protected void sortHelper(int[] array, int[] buf, int left, int right) {
        if (right - left <= threshold) {
            insertSort(array, left, right);
            return;
        }
        PriorityQueue<Partition> queue = new PriorityQueue<>((right - left - 1) / this.threshold + 1);
        queue.offer(new Partition(left, right, false));
        while (queue.size() > 0) {
            Partition part = queue.poll();
            int a = part.a, b = part.b;
            boolean bad = part.bad;
            int pIdx;
            if (bad) pIdx = medOfMed(array, a, b);
            else pIdx = medP3(array, a, b, 1);
            int[] pr = partition(array, buf, a, b, array[pIdx]);
            int lLen = pr[0] - a, rLen = b - pr[1], eqLen = pr[1] - pr[0];
            // if all equal, skip this partition
            if (eqLen == b - a) continue;
            if (rLen == 0) {
                consumePartition(array, queue, a, pr[0], eqLen < lLen / 8);
                continue;
            }
            if (lLen == 0) {
                consumePartition(array, queue, pr[1], b, eqLen < rLen / 8);
                continue;
            }
            bad = rLen / 8 > lLen || lLen / 8 > rLen;
            consumePartition(array, queue, a, pr[0], bad);
            consumePartition(array, queue, pr[1], b, bad);
        }
    }

    /**
     * Sorts the range {@code [a, b)} of {@code array} using a Median-of-Medians
     * Out-of-Place Stable Quicksort.
     * 
     * @param array the array
     * @param a     the start of the range, inclusive
     * @param b     the end of the range, exclusive
     */
    public void quickSort(int[] array, int a, int b) {
        int len = b - a;
        int balance = 0, eq = 0, streaks = 0, dist, eqdist, loop, cnt = len, pos = a;
        while (cnt > 16) {
            for (eqdist = dist = 0, loop = 0; loop < 16; loop++) {
                int cmp = Reads.compareIndices(array, pos, pos + 1, 0.5, true);
                dist += cmp > 0 ? 1 : 0;
                eqdist += cmp == 0 ? 1 : 0;
                pos++;
            }
            streaks += equ(dist, 0) | equ(dist + eqdist, 16);
            balance += dist;
            eq += eqdist;
            cnt -= 16;
        }
        while (--cnt > 0) {
            int cmp = Reads.compareIndices(array, pos, pos + 1, 0.5, true);
            balance += cmp > 0 ? 1 : 0;
            eq += cmp == 0 ? 1 : 0;
            pos++;
        }
        if (balance == 0) return;
        if (balance + eq == len - 1) {
            if (eq > 0) stableSegmentReversal(array, a, b - 1);
            else if (b - a < 4) Writes.swap(array, a, b - 1, 0.75, true, false);
            else Writes.reversal(array, a, b - 1, 0.75, true, false);
            return;
        }
        int[] buf = Writes.createExternalArray(len);
        int sixth = len / 6;
        if (streaks > len / 20 || balance <= sixth || balance + eq >= len - sixth)
            mergeSort(array, buf, a, b);
        else sortHelper(array, buf, a, b);
        Writes.deleteExternalArray(buf);
    }

    @Override
    public void runSort(int[] array, int sortLength, int bucketCount) {
        quickSort(array, 0, sortLength);

    }

}
