/* Copyright (c) 2012, Kaggle
 * Author: Ben Hamner (ben@benhamner.com)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.unidue.langtech.grading.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class MQWKTest
{
    
    private static final double EPSILON = 0.00001;
    
    @Test
    public void confusionMatrixTest() {
        
        ConfusionMatrix cf1 = new ConfusionMatrix(
                Arrays.asList(new Integer[]{1,2}),
                Arrays.asList(new Integer[]{1,2}),
                1,2
        );
        cf1.printConfusionMatrix();
        
        assertEquals("1,0,0,1", cf1.getConfusionMatrixSerialization());
        
        ConfusionMatrix cf2 = new ConfusionMatrix(
                Arrays.asList(new Integer[]{1,2}),
                Arrays.asList(new Integer[]{1,2}),
                0,1,2
        );
        cf2.printConfusionMatrix();
        
        assertEquals("0,0,0,0,1,0,0,0,1", cf2.getConfusionMatrixSerialization());
        
        
        ConfusionMatrix cf3 = new ConfusionMatrix(
                Arrays.asList(new Integer[]{1,1,2,2,4}),
                Arrays.asList(new Integer[]{1,1,3,3,5}),
                1,2,3,4,5
        );
        cf3.printConfusionMatrix();

        assertEquals("2,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0", cf3.getConfusionMatrixSerialization());
        
        ConfusionMatrix cf4 = new ConfusionMatrix(
                Arrays.asList(new Integer[]{1,2}),
                Arrays.asList(new Integer[]{1,2}),
                1,2,3,4
        );
        cf4.printConfusionMatrix();

        assertEquals("1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0", cf4.getConfusionMatrixSerialization());

    }
    
    @Test
    public void quadraticWeightedKappaTest() {
        assertEquals(
                QuadraticWeightedKappa.getKappa(
                        new Integer[]{1,2,3},
                        new Integer[]{1,2,3},
                        1,2,3
                ),
                1.0,
                EPSILON
        );

        assertEquals(
                QuadraticWeightedKappa.getKappa(
                        new Integer[]{1,2,1},
                        new Integer[]{1,2,2},
                        1,2
                ),
                0.4,
                EPSILON
        );

        assertEquals(
                QuadraticWeightedKappa.getKappa(
                        new Integer[]{1,2,3,1,2,2,3},
                        new Integer[]{1,2,3,1,2,3,2},
                        0,1,2,3
                ),
                0.75,
                EPSILON
        );
        
        assertEquals(
                QuadraticWeightedKappa.getKappa(
                        new Integer[]{1,2,3,1,2,2,3},
                        new Integer[]{1,2,3,1,2,3,2},
                        3,2,1,0
                ),
                0.75,
                EPSILON
        );
    }

    @Test
    public void meanQuadraticWeightedKappaTest() {
        assertEquals(0.999, QuadraticWeightedKappa.getMeanKappa(new Double[]{1.0,1.0}), EPSILON);

        assertEquals(0.0, QuadraticWeightedKappa.getMeanKappa(new Double[]{-1.0,1.0}), EPSILON);

        assertEquals(0.67722, QuadraticWeightedKappa.getMeanKappa(new Double[]{0.5,0.8}), EPSILON);

        assertEquals(0.62454, QuadraticWeightedKappa.getMeanWeightedKappa(new Double[]{0.5,0.8}, new Double[]{1.0,0.5}), EPSILON);
    }
    
    @Test
    public void getKappa() {
        assertEquals(0.333333, QuadraticWeightedKappa.getKappa(new Integer[]{1,2,3,4}, new Integer[]{2,3,2,3}, 1,2,3,4), EPSILON);
        assertEquals(0.5, QuadraticWeightedKappa.getKappa(new Integer[]{1,1,2,2}, new Integer[]{1,1,1,2}, 1,2), EPSILON);
        assertEquals(0.5, QuadraticWeightedKappa.getKappa(new Integer[]{1,1,0,0}, new Integer[]{1,1,1,0}, 0,1), EPSILON);
        assertEquals(0.5, QuadraticWeightedKappa.getKappa(new Integer[]{1,1,0,0}, new Integer[]{1,0,0,0}, 0,1), EPSILON);
        assertEquals(0.6, QuadraticWeightedKappa.getKappa(new Integer[]{1,1,0,0,0,0,0,0}, new Integer[]{1,0,0,0,0,0,0,0}, 0,1), EPSILON);
        assertEquals(0.6, QuadraticWeightedKappa.getKappa(new Integer[]{1,1,2,2,2,2,2,2}, new Integer[]{1,2,2,2,2,2,2,2}, 1,2), EPSILON);
        assertEquals(0.0, QuadraticWeightedKappa.getKappa(
        		new Integer[]{1,1,1,1,1,1,1,1,1,1,1,1,1},
        		new Integer[]{1,2,1,3,1,1,1,1,1,1,1,1,1},
        		1,2,3), EPSILON);
    }
}
