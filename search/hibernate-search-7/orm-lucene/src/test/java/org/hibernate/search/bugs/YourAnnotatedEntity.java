package org.hibernate.search.bugs;

import org.hibernate.annotations.TenantId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

@Entity
@Indexed
public class YourAnnotatedEntity {

	@Id
	@DocumentId
	private Long id;

	@FullTextField(analyzer = "nameAnalyzer")
	private String name;

	@TenantId
	private String tenantName;

	protected YourAnnotatedEntity() {
	}

	public YourAnnotatedEntity(Long id, String name, String tenantName) {
		this.id = id;
		this.name = name;
		this.tenantName = tenantName;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTenantName() {
		return tenantName;
	}
}
