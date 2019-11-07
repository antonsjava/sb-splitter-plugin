/*
 * Copyright 2019 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.sbsplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Match string with pattern. Pattern can use wild cards '*' and '?'.
 * Character '?' stands for any character (exactly one). Character '*' 
 * stands for sequence of characters (possibly empty sequence). 
 * @author antons
 */
public class WildMatcher {
    private String pattern;
    private List<Element> elements = new ArrayList<Element>();
    

    public WildMatcher(String pattern) {
        this.pattern = pattern;
        preprocessPattern();
    }

    public static WildMatcher instance(String pattern) {
        return new WildMatcher(pattern);
    }

    private void preprocessPattern() {
        int lastindex = 0;
        boolean asterixBefore = false;
        for(int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if(c == '*') {
                if(!asterixBefore) {
                    asterixBefore = true;
                    if((i-lastindex) > 0) {
                        elements.add(TextElement.instance(pattern, lastindex, i - lastindex));
                    }
                    elements.add(AsterixElement.instance());
                }
                lastindex = i+1;
            } else if(c == '?') {
                asterixBefore = false;
                if((i-lastindex) > 0) {
                    elements.add(TextElement.instance(pattern, lastindex, i - lastindex));
                }
                elements.add(QuestionmarkElement.instance());
                lastindex = i+1;
            } else {
                asterixBefore = false;
            }
        }
        if((pattern.length()-lastindex) > 0) {
            elements.add(TextElement.instance(pattern, lastindex, pattern.length() - lastindex));
        }
    }

    public boolean match(String text) {
        if(text == null) return false;
        if(text.length() == 0) return false;
        return tryMatch(text, 0, 0);
    }
    
    public boolean tryMatch(String text, int element, int offset) {
        if(element >= elements.size()) {
            if(offset == text.length()) return true;
            else return false;
        } else {
            Element el = elements.get(element);
            int min = el.minLength();
            int max = el.maxLength(text.length() - offset);
            for(int i = max; i >= min; i--) {
                if(el.match(text, offset, i)) {
                    if(tryMatch(text, element+1, offset+i)) return true;
                }
            }
            return false;
        }
        
    }
    
//    public static void main(String[] argv) {
//        String pattern = "*j*a*b*lka  a hrus*k*y*";
//        WildMatcher wm = WildMatcher.instance(pattern);
//        System.out.println(" elemets: " + wm.elements);
//        String text = "jablka a hrusky";
//        System.out.println(" text: " + text + " match: " + wm.match(text));
//    }
    
    private static interface Element {
        int minLength();
        int maxLength(int max);
        boolean match(String text, int offset, int length);
    }
    private static class TextElement implements Element {
        private String text;
        private int offset;
        private int length;
        public static TextElement instance(String text, int offset, int length) {
            TextElement element = new TextElement();
            element.text = text; 
            element.offset = offset; 
            element.length = length;
            return element;
        }
        @Override public int minLength() { return length; }
        @Override public int maxLength(int max) { return length; }
        @Override public boolean match(String text, int offset, int length) { 
            if(this.length != length) return false;
            if(text.length() < (offset+length)) return false;
            for(int i = 0; i < length; i++) {
                char c = this.text.charAt(this.offset + i);
                char cc = text.charAt(offset + i);
                if(c != cc) return false;
            }
            return true;
        }
        @Override public String toString() { return text.substring(offset, offset+length); }
    }
    private static class QuestionmarkElement implements Element {
        public static QuestionmarkElement instance() {
            QuestionmarkElement element = new QuestionmarkElement();
            return element;
        }
        @Override public int minLength() { return 1; }
        @Override public int maxLength(int max) { return 1; }
        @Override public boolean match(String text, int offset, int length) { 
            if(length != 1) return false;
            return true;
        }
        @Override public String toString() { return "'?'"; }
    }
    private static class AsterixElement implements Element {
        public static AsterixElement instance() {
            AsterixElement element = new AsterixElement();
            return element;
        }
        @Override public int minLength() { return 0; }
        @Override public int maxLength(int max) { return max; }
        @Override public boolean match(String text, int offset, int length) { 
            return true;
        }
        @Override public String toString() { return "'*'"; }
    }
    
}
