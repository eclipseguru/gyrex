package org.eclipse.gyrex.rap.application;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PageProviderTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private PageProvider provider;

	@Test
	public void addCategory_does_not_allow_null() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("category must not be null");
		provider.addCategory(null);
	}

	@Test
	public void addPage_does_not_allow_null() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("page must not be null");
		provider.addPage(null);
	}

	@Before
	public void setUp() throws Exception {
		provider = new PageProvider() {

			@Override
			public Page createPage(final PageHandle pageHandle) throws Exception {
				return mock(Page.class);
			}
		};
	}

}
