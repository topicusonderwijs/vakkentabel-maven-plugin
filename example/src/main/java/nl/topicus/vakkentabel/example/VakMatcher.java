package nl.topicus.vakkentabel.example;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface VakMatcher
{
	boolean satisfiedBy(@NotNull List<Integer> vakken);

	@NotNull
	Set<Integer> getMatchingSet(@NotNull  List<Integer> vakken);
}
