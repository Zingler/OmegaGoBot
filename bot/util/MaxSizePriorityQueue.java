package util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import lombok.Data;

public class MaxSizePriorityQueue<T> {
    PriorityQueue<Score<T>> queue;
    PriorityQueue<Score<T>> reverseQueue;
    int maxSize;

    public MaxSizePriorityQueue( int maxSize, boolean min ) {
        this.maxSize = maxSize;
        if ( min ) {
            this.queue = new PriorityQueue<>();
            this.reverseQueue = new PriorityQueue<>( Comparator.reverseOrder() );
        } else {
            this.queue = new PriorityQueue<>( Comparator.reverseOrder() );
            this.reverseQueue = new PriorityQueue<>();
        }
    }

    /*
     * Destroys queue when you retrieve everything from it.
     */
    public List<Score<T>> getAll() {
        List<Score<T>> result = new ArrayList<>();
        while ( !this.queue.isEmpty() ) {
            result.add( this.queue.poll() );
        }
        return result;
    }

    public List<T> getAllElements() {
        List<T> result = new ArrayList<>();
        while ( !this.queue.isEmpty() ) {
            result.add( this.queue.poll().getElement() );
        }
        return result;
    }

    public void put( T t, double value ) {
        Score<T> score = new Score<T>( t, value );
        this.queue.add( score );
        this.reverseQueue.add( score );
        if ( this.queue.size() > this.maxSize ) {
            Score<T> poppedScore = this.reverseQueue.poll();
            this.queue.remove( poppedScore );
        }
    }

    @Data
    public static class Score<T> implements Comparable<Score<T>> {
        final T element;
        final double score;

        @Override
        public int compareTo( Score<T> o ) {
            return Double.compare( this.score, o.score );
        }
    }
}
