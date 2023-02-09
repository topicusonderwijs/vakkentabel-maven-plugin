package nl.topicus.vakkentabel.example;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;

public class MatcherFactory
{
	@NotNull
	public static VakMatcher any(int @NotNull ... acceptedCodes)
	{
		return new AnyMatcher(acceptedCodes);
	}

	@NotNull
	public static VakMatcher reject() {
		return new VakMatcher()
		{
			@Override 
			public boolean satisfiedBy(@NotNull List<Integer> vakken)
			{
				return false;
			}

			@Override
			@NotNull
			public Set<Integer> getMatchingSet(@NotNull List<Integer> vakken)
			{
				return Set.of();
			}
		};
	}

	private static class AnyMatcher implements VakMatcher
	{
		private final Set<Integer> acceptedCodes;

		public AnyMatcher(int... acceptedCodes)
		{
			this.acceptedCodes = IntStream.of(acceptedCodes).boxed().collect(Collectors.toSet());
		}

		@Override
		public boolean satisfiedBy(@NotNull List<Integer> vakken)
		{
			return vakken.stream().anyMatch(acceptedCodes::contains);
		}

		@Override
		@NotNull
		public Set<Integer> getMatchingSet(@NotNull List<Integer> vakken)
		{
			return vakken.stream().filter(acceptedCodes::contains).collect(Collectors.toSet());
		}
	}
}
