package fishtail.ecom.service;

import fishtail.ecom.dto.ContentSnippetDTO;
import fishtail.ecom.entity.ContentSnippet;
import fishtail.ecom.repository.ContentSnippetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentSnippetService {

    private final ContentSnippetRepository snippetRepository;

    public ContentSnippetDTO createSnippet(ContentSnippetDTO dto) {
        ContentSnippet snippet = ContentSnippet.builder()
                .name(dto.getName())
                .content(dto.getContent())
                .build();
        return toDTO(snippetRepository.save(snippet));
    }

    public List<ContentSnippetDTO> getAllSnippets() {
        return snippetRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ContentSnippetDTO getSnippetById(Long id) {
        ContentSnippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snippet not found"));
        return toDTO(snippet);
    }

    public ContentSnippetDTO updateSnippet(Long id, ContentSnippetDTO dto) {
        ContentSnippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snippet not found"));
        snippet.setName(dto.getName());
        snippet.setContent(dto.getContent());
        return toDTO(snippetRepository.save(snippet));
    }

    public void deleteSnippet(Long id) {
        if (!snippetRepository.existsById(id)) {
            throw new RuntimeException("Snippet not found");
        }
        snippetRepository.deleteById(id);
    }

    private ContentSnippetDTO toDTO(ContentSnippet entity) {
        return ContentSnippetDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
