package pk.edu.kics.utill;

import static java.util.stream.Collectors.toList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TypeUtil {

	public static Concept mergeConcept(Concept dst, Concept src) {
		List<String> names = Stream.concat(dst.getNames().stream(), src.getNames().stream())
	            .filter(Objects::nonNull).distinct().collect(toList());
	    dst.setNames(names);
	    List<String> ids = Stream.concat(dst.getids().stream(), src.getids().stream())
	            .filter(Objects::nonNull).distinct().collect(toList());
	    dst.setIds(ids);
	    List<SemanticType> semanticTypes = Stream
	            .concat(dst.getTypes().stream(), src.getTypes().stream())
	            .filter(Objects::nonNull).distinct().collect(toList());
	    dst.setTypes(semanticTypes);
	    List<ConceptMention> mentions = Stream
	            .concat(dst.getMentions().stream(), src.getMentions().stream())
	            .filter(Objects::nonNull).distinct().collect(toList());
	    dst.setMentions(mentions);
	    
	    return dst;
	}
}
