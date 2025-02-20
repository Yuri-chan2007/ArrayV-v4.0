package utils;

import java.util.Arrays;

import main.ArrayVisualizer;
import panes.JErrorPane;

/*
 *
MIT License

Copyright (c) 2019 w0rthy

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

final public class Highlights {
    private volatile int[] highlights;
    private volatile byte[] markCounts;

    private volatile int maxHighlightMarked;    // IMPORTANT: This stores the index one past the farthest highlight used, so that a value
                                                // of 0 means no highlights are in use, and iteration is more convenient.

                                                // The Highlights array is huge and slows down the visualizer if all its indices are read.
                                                // In an attempt to speed up scanning through all highlights while also giving anyone room
                                                // to use the full array, this variable keeps track of the farthest highlight in use. The
                                                // Highlights array thus only needs to be scanned up to index maxHighightMarked.

                                                // If an highlight is used with markArray() that is higher than maxPossibleMarked, the
                                                // variable is updated. If the farthest highlight is removed with clearMark(), the next
                                                // farthest highlight is found and updates maxIndexMarked.

                                                // Trivially, clearAllMarks() resets maxIndexMarked to zero. This variable also serves
                                                // as a subtle design hint for anyone who wants to add an algorithm to the app to highlight
                                                // array positions at low indices which are close together.

                                                // This way, the program runs more efficiently, and looks pretty. :)

    private volatile int markCount;

    private boolean FANCYFINISH;
    private volatile boolean fancyFinish;
    private volatile int trackFinish;

    private ArrayVisualizer ArrayVisualizer;

    public Highlights(ArrayVisualizer ArrayVisualizer, int maximumLength) {
        this.ArrayVisualizer = ArrayVisualizer;

        try {
            this.highlights = new int[maximumLength];
            this.markCounts = new byte[maximumLength];
        } catch (OutOfMemoryError e) {
            JErrorPane.invokeCustomErrorMessage("Failed to allocate mark arrays. The program will now exit.");
            System.exit(1);
        }
        this.FANCYFINISH = true;
        this.maxHighlightMarked = 0;
        this.markCount = 0;

        Arrays.fill(highlights, -1);
        Arrays.fill(markCounts, (byte)0);
    }

    public boolean fancyFinishEnabled() {
        return this.FANCYFINISH;
    }
    public void toggleFancyFinishes(boolean Bool) {
        this.FANCYFINISH = Bool;
    }

    public boolean fancyFinishActive() {
        return this.fancyFinish;
    }
    public void toggleFancyFinish(boolean Bool) {
        this.fancyFinish = Bool;
    }

    public int getFancyFinishPosition() {
        return this.trackFinish;
    }
    public void incrementFancyFinishPosition() {
        this.trackFinish++;
    }
    public void resetFancyFinish() {
        this.trackFinish = -1; // Magic number that clears the green sweep animation
    }

    public void toggleAnalysis(boolean Bool) {
        this.ArrayVisualizer.toggleAnalysis(Bool);
    }

    public int getMaxHighlight() {
        return this.maxHighlightMarked;
    }
    public int getMarkCount() {
        return this.markCount;
    }

    private void incrementIndexMarkCount(int i) {
        if (i >= markCounts.length) return;
        if (markCounts[i] != (byte)-1) {
            if (markCounts[i] == 0) {
                markCount++;
            }
            markCounts[i]++;
        }
    }
    private void decrementIndexMarkCount(int i) {
        if (i >= markCounts.length) return;
        if (markCounts[i] == (byte)-1) {
            int count = 0;
            for (int h = 0; h < this.maxHighlightMarked; h++) {
                if (highlights[h] == i) {
                    count++;
                    if (count > 255) {
                        return;
                    }
                }
            }
        } else if (markCounts[i] == 0) {
            markCount--;
        }
        markCounts[i]--;
    }

    //Consider revising highlightList().
    public int[] highlightList() {
        return this.highlights;
    }
    public boolean containsPosition(int arrayPosition) {
        if (arrayPosition >= markCounts.length) return false;
        return this.markCounts[arrayPosition] != 0;
    }
    public synchronized void markArray(int marker, int markPosition) {
        try {
            if (markPosition < 0) {
                if (markPosition == -1) throw new Exception("Highlights.markArray(): Invalid position! -1 is reserved for the clearMark method.");
                else if (markPosition == -5) throw new Exception("Highlights.markArray(): Invalid position! -5 was the constant originally used to unmark numbers in the array. Instead, use the clearMark method.");
                else throw new Exception("Highlights.markArray(): Invalid position!");
            } else {
                if (highlights[marker] == markPosition) {
                    return;
                }
                if (highlights[marker] != -1) {
                    decrementIndexMarkCount(highlights[marker]);
                }
                highlights[marker] = markPosition;
                incrementIndexMarkCount(markPosition);

                if (marker >= this.maxHighlightMarked) {
                    this.maxHighlightMarked = marker + 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayVisualizer.updateNow();
    }
    public synchronized void clearMark(int marker) {
        if (highlights[marker] == -1) {
            return;
        }
        decrementIndexMarkCount(highlights[marker]);
        highlights[marker] = -1; // -1 is used as the magic number to unmark a position in the main array

        if (marker == this.maxHighlightMarked) {
            this.maxHighlightMarked = marker;
            while (maxHighlightMarked > 0 && highlights[maxHighlightMarked-1] == -1) {
                maxHighlightMarked--;
            }
        }
        ArrayVisualizer.updateNow();
    }
    public synchronized void clearAllMarks() {
        for (int i = 0; i < this.maxHighlightMarked; i++) {
            if (highlights[i] != -1) {
                markCounts[highlights[i]] = 0;
            }
        }
        Arrays.fill(this.highlights, 0, this.maxHighlightMarked, -1);
        this.maxHighlightMarked = 0;
        this.markCount = 0;
        ArrayVisualizer.updateNow();
    }
}