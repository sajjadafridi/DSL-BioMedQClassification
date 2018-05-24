package edu.emory.clir.clearnlp.utill;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
	    List<Type> types = Stream
	            .concat(dst.getTypes().stream(), src.getTypes().stream())
	            .filter(Objects::nonNull).distinct().collect(toList());
	    dst.setTypes(types);
	    List<ConceptMention> mentions = Stream
	            .concat(dst.getMentions().stream(), src.getMentions().stream())
	            .filter(Objects::nonNull).distinct().collect(toList());
	    dst.setMentions(mentions);
	    
	    return dst;
	}
}
