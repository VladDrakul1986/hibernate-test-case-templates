package org.hibernate.search.bugs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.search.util.common.impl.CollectionHelper.asSet;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.orm.session.SearchSession;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class YourIT extends SearchTestBase {

	@Override
	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { YourAnnotatedEntity.class };
	}

	// This works as expected. We fill the database from Scratch and the different tenants are handled correctly without mass indexing
	@Test
	public void testYourBug() {
		String myLocalTenantName = "mainTenant";
		TenantContext.setCurrentTenant(myLocalTenantName);
		try ( Session s = getSessionFactory().openSession() ) {
			YourAnnotatedEntity yourEntity1 = new YourAnnotatedEntity( 1L, "Jane Smith", myLocalTenantName );
			YourAnnotatedEntity yourEntity2 = new YourAnnotatedEntity( 2L, "John Doe", myLocalTenantName );

			Transaction tx = s.beginTransaction();
			s.persist( yourEntity1 );
			s.persist( yourEntity2 );
			tx.commit();
		}
		try ( Session session = getSessionFactory().openSession() ) {
			SearchSession searchSession = Search.session( session );

			SearchResult<YourAnnotatedEntity> searchResult = searchSession.search( YourAnnotatedEntity.class )
					.where( f -> f.match().field( "name" ).matching( "smith" ) )
					.fetch(5);
			Assertions.assertEquals(1, searchResult.total().hitCount());
			assertThat( searchResult.hits() )
					.hasSize( 1 )
					.element( 0 ).extracting( YourAnnotatedEntity::getId )
					.isEqualTo( 1L );
		}
		TenantContext.setCurrentTenant("anotherTenant");
		try ( Session session = getSessionFactory().openSession() ) {
			SearchSession searchSession = Search.session( session );

			SearchResult<YourAnnotatedEntity> searchResult = searchSession.search( YourAnnotatedEntity.class )
					.where( f -> f.match().field( "name" ).matching( "smith" ) )
					.fetch(5);

			assertThat( searchResult.hits() )
					.hasSize( 0 );
			Assertions.assertEquals(0, searchResult.total().hitCount());
		}
	}

	// This works NOT as expected. We fill the database from Scratch and perform a mass index on that database.
	// The different tenant information provided in the database are ignored by the mass indexer which results in a false hitcount
	// This leads to differences between the expected hitcount and the fetched entities from our database
	@Test
	public void testYourBugMassIndexer() throws InterruptedException {
		String myLocalTenantName = "mainTenant";
		TenantContext.setCurrentTenant(myLocalTenantName);
		try ( Session s = getSessionFactory().openSession() ) {
			YourAnnotatedEntity yourEntity1 = new YourAnnotatedEntity( 1L, "Jane Smith", myLocalTenantName );
			YourAnnotatedEntity yourEntity2 = new YourAnnotatedEntity( 2L, "John Doe", myLocalTenantName );

			Transaction tx = s.beginTransaction();
			s.persist( yourEntity1 );
			s.persist( yourEntity2 );
			tx.commit();
		}
		SearchMapping searchMapping = Search.mapping( getSessionFactory() );
		searchMapping.scope( Object.class ).massIndexer( asSet( "mainTenant", "anotherTenant" )).purgeAllOnStart(true).startAndWait();

		try ( Session session = getSessionFactory().openSession() ) {
			SearchSession searchSession = Search.session( session );

			SearchResult<YourAnnotatedEntity> searchResult = searchSession.search( YourAnnotatedEntity.class )
					.where( f -> f.match().field( "name" ).matching( "smith" ) )
					.fetch(5);
			Assertions.assertEquals(1, searchResult.total().hitCount());
			assertThat( searchResult.hits() )
					.hasSize( 1 )
					.element( 0 ).extracting( YourAnnotatedEntity::getId )
					.isEqualTo( 1L );
		}
		TenantContext.setCurrentTenant("anotherTenant");
		try ( Session session = getSessionFactory().openSession() ) {
			SearchSession searchSession = Search.session( session );

			SearchResult<YourAnnotatedEntity> searchResult = searchSession.search( YourAnnotatedEntity.class )
					.where( f -> f.match().field( "name" ).matching( "smith" ) )
					.fetch(5);

			assertThat( searchResult.hits() )
					.hasSize( 0 );
			Assertions.assertEquals(0, searchResult.total().hitCount());
		}
	}

}
