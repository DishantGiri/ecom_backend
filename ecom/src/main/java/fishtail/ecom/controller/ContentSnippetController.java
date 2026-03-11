package fishtail.ecom.controller;

import fishtail.ecom.dto.ContentSnippetDTO;
import fishtail.ecom.service.ContentSnippetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/snippets")
@RequiredArgsConstructor
public class ContentSnippetController {

    private final ContentSnippetService snippetService;

    @PostMapping
    public ResponseEntity<ContentSnippetDTO> createSnippet(@RequestBody ContentSnippetDTO dto) {
        return ResponseEntity.ok(snippetService.createSnippet(dto));
    }

    @GetMapping
    public ResponseEntity<List<ContentSnippetDTO>> getAllSnippets() {
        return ResponseEntity.ok(snippetService.getAllSnippets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentSnippetDTO> getSnippetById(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.getSnippetById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentSnippetDTO> updateSnippet(@PathVariable Long id, @RequestBody ContentSnippetDTO dto) {
        return ResponseEntity.ok(snippetService.updateSnippet(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSnippet(@PathVariable Long id) {
        snippetService.deleteSnippet(id);
        return ResponseEntity.ok("Snippet deleted successfully");
    }
}
