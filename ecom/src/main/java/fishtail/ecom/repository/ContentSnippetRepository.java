package fishtail.ecom.repository;

import fishtail.ecom.entity.ContentSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentSnippetRepository extends JpaRepository<ContentSnippet, Long> {
}
