/**
 * Created on Jul 6, 2007
 */
package bias.extension.SimpleSearch;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bias.utils.Validator;

/**
 * @author kion
 */
public class SearchEngine {
    
    public static class HighLightMarker {
        private Integer beginIndex;
        private Integer endIndex;
        public HighLightMarker() {
        }
        public HighLightMarker(Integer beginIndex, Integer endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }
        public Integer getBeginIndex() {
            return beginIndex;
        }
        public void setBeginIndex(Integer beginIndex) {
            this.beginIndex = beginIndex;
        }
        public Integer getEndIndex() {
            return endIndex;
        }
        public void setEndIndex(Integer endIndex) {
            this.endIndex = endIndex;
        }
    }
    
    public static Map<UUID, Map<String, HighLightMarker>> search(SearchCriteria sc, Map<UUID, Collection<String>> entries) throws Throwable {
        Map<UUID, Map<String, HighLightMarker>> matchesFound = new LinkedHashMap<UUID, Map<String, HighLightMarker>>();
        if (sc != null && entries != null && !entries.isEmpty()) {
            Pattern pattern = null;
            if (sc.isRegularExpression()) {
                pattern = Pattern.compile(sc.getSearchExpression());
            }
            for (Entry<UUID, Collection<String>> entry : entries.entrySet()) {
                Collection<String> searchData = entry.getValue();
                if (searchData != null) {
                    if (pattern != null) {
                        for (String searchDataPiece : searchData) {
                            if (!Validator.isNullOrBlank(searchDataPiece)){
                                Matcher matcher = pattern.matcher(searchDataPiece);
                                if (matcher.find()) {
                                    Map<String, HighLightMarker> stringsFound = matchesFound.get(entry.getKey());
                                    if (stringsFound == null) {
                                        stringsFound = new LinkedHashMap<String, HighLightMarker>();
                                    }
                                    int index = matcher.start();
                                    int length = matcher.end() - matcher.start();
                                    Integer hlIndex = null;
                                    int scope = (searchDataPiece.length() - length) > 50 ? 
                                            50 : searchDataPiece.length() - length;
                                    int beginIndex;
                                    int endIndex;
                                    if (index <= 25) {
                                        beginIndex = 0;
                                        endIndex = scope + length;
                                        hlIndex = index;
                                    } else {
                                        beginIndex = index - scope/2;
                                        endIndex = index + scope/2 + length;
                                        hlIndex = index - beginIndex;
                                    }
                                    searchDataPiece = searchDataPiece.substring(beginIndex, endIndex);
                                    stringsFound.put(searchDataPiece, new HighLightMarker(hlIndex, hlIndex + length));
                                    matchesFound.put(entry.getKey(), stringsFound);
                                }
                            }
                        }
                    } else {
                        for (String searchDataPiece : searchData) {
                            if (!Validator.isNullOrBlank(searchDataPiece)){
                                int index = -1;
                                if (sc.isCaseSensitive()) {
                                    index = searchDataPiece.indexOf(sc.getSearchExpression());
                                } else {
                                    index = searchDataPiece.toLowerCase().indexOf(sc.getSearchExpression().toLowerCase());
                                }
                                if (index != -1) {
                                    Map<String, HighLightMarker> stringsFound = matchesFound.get(entry.getKey());
                                    if (stringsFound == null) {
                                        stringsFound = new LinkedHashMap<String, HighLightMarker>();
                                    }
                                    Integer hlIndex = null;
                                    int scope = (searchDataPiece.length() - sc.getSearchExpression().length()) > 50 ? 
                                            50 : searchDataPiece.length() - sc.getSearchExpression().length();
                                    int beginIndex;
                                    int endIndex;
                                    if (index <= 25) {
                                        beginIndex = 0;
                                        endIndex = scope + sc.getSearchExpression().length();
                                        hlIndex = index;
                                    } else {
                                        beginIndex = index - scope/2;
                                        endIndex = index + scope/2 + sc.getSearchExpression().length();
                                        hlIndex = index - beginIndex;
                                    }
                                    searchDataPiece = searchDataPiece.substring(beginIndex, endIndex); 
                                    stringsFound.put(searchDataPiece, new HighLightMarker(hlIndex, hlIndex + sc.getSearchExpression().length()));
                                    matchesFound.put(entry.getKey(), stringsFound);
                                }
                            }
                        }
                    }
                }
            }
        }
        return matchesFound;    
    }    
    
}
