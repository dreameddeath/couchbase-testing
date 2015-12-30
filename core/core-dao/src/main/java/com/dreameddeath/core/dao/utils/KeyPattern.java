/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.dao.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 28/12/2015.
 */
public class KeyPattern {
    private final String keyRawPattern;
    private final String matchingKeyPatternStr;
    private final Pattern matchingKeyPattern;
    private final List<String> parameters=new ArrayList<>();


    public KeyPattern(String rawPattern){
        this.keyRawPattern =rawPattern;
        this.matchingKeyPatternStr=compileKey();
        this.matchingKeyPattern = Pattern.compile(matchingKeyPatternStr);
    }

    public String getKeyPatternStr(){
        return matchingKeyPatternStr;
    }

    public Pattern getKeyPattern(){
        return matchingKeyPattern;
    }

    public List<String> getKeyParameters() {
        return parameters;
    }


    public String[] extractParamsArrayFromKey(String key) {
        String[] result =new String[parameters.size()];
        Matcher matcher = matchingKeyPattern.matcher(key);
        if(matcher.matches()){
            for(int groupPos=0;groupPos<matcher.groupCount();++groupPos){
                result[groupPos]=matcher.group(groupPos+1);
            }
        }
        return result;
    }

    public Map<String,String> extractParamsFromKey(String key){
        Map<String,String> result = new TreeMap<>();
        Matcher matcher = matchingKeyPattern.matcher(key);
        if(matcher.matches()){
            for(int groupPos=0;groupPos<matcher.groupCount();groupPos++){
                result.put(parameters.get(groupPos),matcher.group(groupPos+1));
            }
        }
        return result;
    }

    protected String compileKey(){
        parameters.clear();
        StringBuilder currPattern=new StringBuilder(keyRawPattern.length());
        StringBuilder currVariableParsingStack=new StringBuilder(keyRawPattern.length());
        ParsingState parsingState=ParsingState.KEY;
        int nbRegexpOpenedBrackets=0;
        final int len = keyRawPattern.length();
        for(int pos=0;pos<len;++pos){
            char value = keyRawPattern.charAt(pos);
            switch (parsingState){
                case KEY:
                    if(value=='{'){
                        parsingState=ParsingState.PARAM;
                        currVariableParsingStack.setLength(0);
                    }
                    else{
                        currPattern.append(value);
                    }
                    break;
                case PARAM:
                    if(value==':'){
                        parameters.add(currVariableParsingStack.toString().trim());
                        currVariableParsingStack.setLength(0);
                        parsingState=ParsingState.PARAM_PATTERN;
                    }
                    else if(value=='}'){
                        parameters.add(currVariableParsingStack.toString().trim());
                        currPattern.append("([^/]+)");
                        parsingState=ParsingState.KEY;
                    }
                    else{
                        currVariableParsingStack.append(value);
                    }
                    break;
                case PARAM_PATTERN:
                    if(value=='{'){
                        nbRegexpOpenedBrackets++;
                        currVariableParsingStack.append(value);
                    }
                    else if(value=='}'){
                        if(nbRegexpOpenedBrackets>0){
                            nbRegexpOpenedBrackets--;
                            currVariableParsingStack.append(value);
                        }
                        else {
                            currPattern
                                    .append("(")
                                    .append(currVariableParsingStack.toString().trim())
                                    .append(")");
                            parsingState = ParsingState.KEY;
                        }
                    }
                    else{
                        currVariableParsingStack.append(value);
                    }
                    break;
            }
        }
        if(parsingState!=ParsingState.KEY){
            throw new IllegalStateException("The pattern "+keyRawPattern+" is invalid");
        }

        return currPattern.toString().trim();
    }


    public enum ParsingState {
        KEY,
        PARAM,
        PARAM_PATTERN
    }
}
