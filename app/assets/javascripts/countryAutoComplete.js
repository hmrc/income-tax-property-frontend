document.addEventListener('DOMContentLoaded', function() {

  const form = document.querySelector('form');
  const visibleInput = document.getElementById('country-autocomplete');
  const hiddenSelect = document.getElementById('country-autocomplete-select');
  const suggestionMenu = document.getElementById('country-autocomplete__listbox');

  if (!form || !visibleInput || !hiddenSelect || !suggestionMenu) {
    return;
  }

  // convert the rendered HTML <option> items into an array
  const optionData = Array.from(hiddenSelect.options)
    .map(opt => ({
      text: opt.text.trim(),
      value: opt.value
    }))
    .filter(item => item.value !== "");

  // Auto-select country if exact match (ignoring case)
  function autoSelectMatchingCountry() {
    const typedName = visibleInput.value.trim().toLowerCase();
    if (!typedName){
        hiddenSelect.value = ""
    } else {
        const matched = optionData.find(item =>
          item.text.toLowerCase() === typedName
        );
        hiddenSelect.value = matched ? matched.value : "";
    }
  }

  // On normal form submit, do a final match
  form.addEventListener('submit', function(e) {
    autoSelectMatchingCountry();
  });

  // On Enter key, pick the first suggestion & submit
  visibleInput.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
      const firstSuggestion = suggestionMenu.querySelector(
        'li.autocomplete__option[role="option"]'
      );
      if (firstSuggestion) {
        visibleInput.value = firstSuggestion.textContent.trim();
      }

      autoSelectMatchingCountry();

      e.preventDefault();
      form.submit();
    }
  });
});