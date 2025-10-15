;(function () {
  // Helper: get cookie value
  function getCookie(name) {
    const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'))
    return match ? decodeURIComponent(match[1]) : null
  }

  // Helper: set cookie (days expiration)
  function setCookie(name, value, days) {
    let expires = ''
    if (typeof days === 'number') {
      const d = new Date()
      d.setTime(d.getTime() + days * 24 * 60 * 60 * 1000)
      expires = '; expires=' + d.toUTCString()
    }
    document.cookie = name + '=' + encodeURIComponent(value) + expires + '; path=/'
  }

  // Normalize language codes to 'vi' or 'en' (fallback to 'en')
  function normalizeLang(code) {
    if (!code) return 'en'
    code = code.toLowerCase()
    if (code.startsWith('vi')) return 'vi'
    if (code.startsWith('en')) return 'en'
    // Add other mappings if you support more languages
    return 'en'
  }

  function updateLangLabel(el, lang) {
    if (!el) return
    if (lang === 'vi') el.textContent = 'VI'
    else if (lang === 'en') el.textContent = 'EN'
    else el.textContent = 'Lang'
  }

  // Read current lang: priority -> URL param 'lang' -> cookie 'lang' -> navigator.language -> default 'en'
  function currentLangFromContext() {
    try {
      const urlParams = new URLSearchParams(window.location.search)
      const urlLang = urlParams.get('lang')
      if (urlLang) return normalizeLang(urlLang)

      const cookieLang = getCookie('lang')
      if (cookieLang) return normalizeLang(cookieLang)

      const nav = navigator.language || navigator.userLanguage || ''
      return normalizeLang(nav)
    } catch (e) {
      return 'en'
    }
  }

  // Replace or add 'lang' param and reload preserving other params
  function navigateWithLang(lang) {
    try {
      const url = new URL(window.location.href)
      url.searchParams.set('lang', lang)
      // Set cookie so subsequent navigations without param still keep language
      setCookie('lang', lang, 365)
      window.location.href = url.toString()
    } catch (e) {
      // Fallback: simple redirect
      const sep = window.location.href.indexOf('?') === -1 ? '?' : '&'
      setCookie('lang', lang, 365)
      window.location.href = window.location.href + sep + 'lang=' + encodeURIComponent(lang)
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    const langLabel = document.getElementById('langLabel')
    const langMenu = document.getElementById('langMenu')

    const current = currentLangFromContext()
    updateLangLabel(langLabel, current)

    // If there are explicit elements with data-lang attributes, use them.
    // Otherwise, fall back to anchors inside the langMenu.
    function attachListeners(container) {
      if (!container) return
      // prefer elements that expose data-lang
      const items = Array.from(container.querySelectorAll('[data-lang]'))
      if (items.length === 0) {
        // fallback: use anchors and infer from their text content
        const anchors = Array.from(container.querySelectorAll('a'))
        anchors.forEach(function (a) {
          a.addEventListener('click', function (ev) {
            ev.preventDefault()
            const txt = (a.textContent || '').trim().toLowerCase()
            let lang = txt === 'vi' ? 'vi' : txt === 'en' ? 'en' : null
            if (!lang) {
              // last-resort: try href?lang=...
              const href = a.getAttribute('href') || ''
              const m = href.match(/[?&]lang=(vi|en)/i)
              if (m) lang = normalizeLang(m[1])
            }
            if (lang) {
              updateLangLabel(langLabel, lang)
              navigateWithLang(lang)
            }
          })
        })
      } else {
        // attach using data-lang
        items.forEach(function (el) {
          el.addEventListener('click', function (ev) {
            ev.preventDefault()
            const lang = normalizeLang(el.getAttribute('data-lang'))
            updateLangLabel(langLabel, lang)
            navigateWithLang(lang)
          })
        })
      }
    }

    attachListeners(langMenu)

    // Optional: keep langLabel clickable to open menu on small devices (UX)
    if (langLabel) {
      langLabel.closest('button')?.addEventListener('click', function () {
        // refresh label in case cookie/URL changed externally
        const updated = currentLangFromContext()
        updateLangLabel(langLabel, updated)
      })
    }
  })
})()
