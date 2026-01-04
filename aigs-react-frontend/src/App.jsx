import { useEffect, useMemo, useRef, useState } from 'react'
import './App.css'

const DIFFICULTIES = [
  { id: 1, label: 'Easy', wordLength: 5, hint: 'Warm-up grid' },
  { id: 2, label: 'Medium', wordLength: 6, hint: 'Tighter patterns' },
  { id: 3, label: 'Hard', wordLength: 7, hint: 'Longer lines' },
  { id: 4, label: 'Ludicrous', wordLength: 9, hint: 'Absolute chaos' },
]

const KEYBOARD_ROWS = ['QWERTYUIOP', 'ASDFGHJKL', 'ZXCVBNM']

function App() {
  const [apiBase, setApiBase] = useState(
    () => localStorage.getItem('wordle_api_base') || 'http://localhost:50005'
  )
  const [authMode, setAuthMode] = useState('login')
  const [userName, setUserName] = useState(
    () => localStorage.getItem('wordle_user') || ''
  )
  const [password, setPassword] = useState('')
  const [token, setToken] = useState(
    () => localStorage.getItem('wordle_token') || ''
  )
  const [userExpiry, setUserExpiry] = useState(
    () => localStorage.getItem('wordle_expiry') || ''
  )
  const [difficulty, setDifficulty] = useState(() => {
    const stored = Number(localStorage.getItem('wordle_difficulty'))
    return Number.isNaN(stored) || stored === 0 ? 1 : stored
  })
  const [game, setGame] = useState(null)
  const [options, setOptions] = useState(null)
  const [currentGuess, setCurrentGuess] = useState('')
  const [statusMessage, setStatusMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isBusy, setIsBusy] = useState(false)
  const [modal, setModal] = useState({ open: false, title: '', message: '' })
  const [authModalOpen, setAuthModalOpen] = useState(false)
  const [invalidWordActive, setInvalidWordActive] = useState(false)
  const [invalidWordMessage, setInvalidWordMessage] = useState('')
  const invalidTimeoutRef = useRef(null)
  const [authError, setAuthError] = useState('')
  const [showBackendTools, setShowBackendTools] = useState(() => {
    const stored = localStorage.getItem('wordle_show_backend_tools')
    return stored === null ? true : stored === 'true'
  })

  useEffect(() => {
    localStorage.setItem('wordle_api_base', apiBase)
  }, [apiBase])

  useEffect(() => {
    localStorage.setItem('wordle_user', userName)
  }, [userName])

  useEffect(() => {
    localStorage.setItem('wordle_token', token)
  }, [token])

  useEffect(() => {
    localStorage.setItem('wordle_expiry', userExpiry)
  }, [userExpiry])

  useEffect(() => {
    localStorage.setItem('wordle_difficulty', String(difficulty))
  }, [difficulty])

  useEffect(() => {
    localStorage.setItem('wordle_show_backend_tools', String(showBackendTools))
  }, [showBackendTools])

  useEffect(() => {
    if (!game?.options) {
      setOptions(null)
      return
    }
    try {
      setOptions(JSON.parse(game.options))
    } catch (error) {
      setOptions(null)
    }
  }, [game])

  useEffect(() => {
    return () => {
      if (invalidTimeoutRef.current) {
        window.clearTimeout(invalidTimeoutRef.current)
      }
    }
  }, [])

  const triggerInvalidWord = () => {
    if (invalidTimeoutRef.current) {
      window.clearTimeout(invalidTimeoutRef.current)
    }
    setInvalidWordMessage("Sorry, this word doesn't exist!")
    setInvalidWordActive(false)
    window.requestAnimationFrame(() => {
      setInvalidWordActive(true)
    })
    invalidTimeoutRef.current = window.setTimeout(() => {
      setCurrentGuess('')
      setInvalidWordActive(false)
      setInvalidWordMessage('')
      invalidTimeoutRef.current = null
    }, 1000)
  }

  useEffect(() => {
    if (!token || !game) return
    const handleBeforeUnload = () => {
      try {
        const payload = JSON.stringify({ token })
        const blob = new Blob([payload], { type: 'application/json' })
        navigator.sendBeacon(`${apiBase}/game/quit`, blob)
      } catch (error) {
        // Ignore unload errors to avoid blocking browser close.
      }
    }

    window.addEventListener('beforeunload', handleBeforeUnload)
    return () => window.removeEventListener('beforeunload', handleBeforeUnload)
  }, [token, game, apiBase])

  useEffect(() => {
    const handleKey = (event) => {
      if (!game || game.result !== null) return
      if (isBusy) return
      if (event.target instanceof HTMLElement) {
        const tagName = event.target.tagName
        const isInput =
          tagName === 'INPUT' || tagName === 'TEXTAREA' || event.target.isContentEditable
        if (isInput && event.key !== 'Enter' && event.key !== 'Backspace') {
          return
        }
      }
      const key = event.key.toUpperCase()
      if (key === 'ENTER') {
        if (currentGuess.length === wordLength) {
          submitGuess()
        }
        return
      }
      if (key === 'BACKSPACE') {
        removeLetter()
        return
      }
      if (/^[A-Z]$/.test(key)) {
        addLetter(key)
      }
    }

    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [game, isBusy, currentGuess, options])

  const boardRowsCount = Array.isArray(game?.board) ? game.board.length : null
  const boardColsCount =
    Array.isArray(game?.board) && game.board[0]
      ? game.board[0].length
      : null
  const wordLength =
    boardColsCount ||
    options?.wordLength ||
    DIFFICULTIES.find((item) => item.id === difficulty)?.wordLength ||
    5

  const maxTries = boardRowsCount || options?.maxTries || 6
  const guesses = options?.guesses || []
  const round = options?.round ?? guesses.length

  const letterStatus = useMemo(() => {
    const scores = {}
    guesses.forEach((guess) => {
      guess.forEach((letter) => {
        const key = String(letter.char || '').toUpperCase()
        if (!key) return
        const next = letter.status
        const current = scores[key]
        if (current === 'correct') return
        if (current === 'present' && next === 'absent') return
        scores[key] = next
      })
    })
    return scores
  }, [guesses])

  const boardRows = useMemo(() => {
    return Array.from({ length: maxTries }, (_, rowIndex) => {
      const guess = guesses[rowIndex]
      if (guess) return { type: 'guess', letters: guess }
      if (rowIndex === guesses.length && game?.result === null) {
        return { type: 'active', letters: currentGuess }
      }
      return { type: 'empty', letters: '' }
    })
  }, [guesses, maxTries, currentGuess, game?.result])

  const clearMessages = () => {
    setStatusMessage('')
    setErrorMessage('')
  }

  const sendRequest = async (path, options = {}) => {
    setIsBusy(true)
    clearMessages()
    try {
      const response = await fetch(`${apiBase}${path}`, {
        headers: {
          'Content-Type': 'application/json',
          ...(options.headers || {}),
        },
        ...options,
      })
      const data = await response.json()
      if (!response.ok) {
        throw new Error(data?.error_description || 'Request failed')
      }
      if (data?.error) {
        throw new Error(data.error_description || data.error)
      }
      return data
    } finally {
      setIsBusy(false)
    }
  }

  const handleRegister = async (event) => {
    event.preventDefault()
    try {
      const data = await sendRequest('/users/register', {
        method: 'POST',
        body: JSON.stringify({ userName, password }),
      })
      setStatusMessage(`User ${data.userName} created. Please sign in.`)
      setAuthMode('login')
      setPassword('')
      setAuthError('')
    } catch (error) {
      setErrorMessage(error.message)
      setAuthError(error.message)
    }
  }

  const handleLogin = async (event) => {
    event.preventDefault()
    try {
      const data = await sendRequest('/users/login', {
        method: 'POST',
        body: JSON.stringify({ userName, password }),
      })
      setToken(data.token || '')
      setUserExpiry(data.userExpiry || '')
      setStatusMessage(`Welcome back, ${data.userName}.`)
      setPassword('')
      setAuthModalOpen(false)
      setAuthError('')
    } catch (error) {
      setErrorMessage(error.message)
      setAuthError('Username or password incorrect.')
    }
  }

  const handleLogout = async () => {
    try {
      await quitGame({ silent: true })
      await sendRequest('/users/logout', {
        method: 'POST',
        body: JSON.stringify({ userName }),
      })
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setToken('')
      setUserExpiry('')
      setGame(null)
      setOptions(null)
      setCurrentGuess('')
      setStatusMessage('Signed out.')
    }
  }

  const startNewGame = async () => {
    if (!token) {
      setErrorMessage('Please login or register before starting a game.')
      setAuthMode('login')
      setAuthModalOpen(true)
      return
    }
    try {
      const data = await sendRequest('/game/new', {
        method: 'POST',
        body: JSON.stringify({
          token,
          gameType: 'Wordle',
          difficulty,
        }),
      })
      setGame(data)
      setCurrentGuess('')
      setStatusMessage('New Wordle loaded. Good luck!')
    } catch (error) {
      setErrorMessage(error.message)
    }
  }

  const quitGame = async ({ silent = false } = {}) => {
    if (!token) return
    try {
      if (game) {
        await sendRequest('/game/quit', {
          method: 'POST',
          body: JSON.stringify({ token }),
        })
      }
      setGame(null)
      setOptions(null)
      setCurrentGuess('')
      if (!silent) {
        setStatusMessage('Game closed.')
      }
    } catch (error) {
      if (!silent) {
        setErrorMessage(error.message)
      }
    }
  }

  const submitGuess = async () => {
    if (!game || game.result !== null) return
    if (currentGuess.length !== wordLength) {
      setErrorMessage(`Guess must be ${wordLength} letters long.`)
      return
    }
    try {
      const data = await sendRequest('/game/move', {
        method: 'POST',
        body: JSON.stringify({
          token,
          guess: currentGuess.toLowerCase(),
        }),
      })
      setGame(data)
      let parsedOptions = null
      try {
        parsedOptions = data?.options ? JSON.parse(data.options) : null
      } catch (error) {
        parsedOptions = null
      }
      if (parsedOptions?.currentWordExists === false) {
        triggerInvalidWord()
        return
      }
      setCurrentGuess('')
      if (data.result === true) {
        setModal({
          open: true,
          title: 'You won!',
          message: 'Perfect hit. You cracked it!',
        })
        await quitGame({ silent: true })
      } else if (data.result === false) {
        setModal({
          open: true,
          title: 'Game over',
          message: `No more tries left. The word was ${String(
            parsedOptions?.solutionWord || 'unknown'
          ).toUpperCase()}.`,
        })
        await quitGame({ silent: true })
      }
    } catch (error) {
      setErrorMessage(error.message)
    }
  }

  const addLetter = (letter) => {
    if (!game || game.result !== null) return
    if (currentGuess.length >= wordLength) return
    setCurrentGuess((prev) => `${prev}${letter}`)
  }

  const removeLetter = () => {
    setCurrentGuess((prev) => prev.slice(0, -1))
  }

  const handlePing = async () => {
    try {
      const data = await sendRequest('/ping')
      setStatusMessage(`Ping OK: ${JSON.stringify(data)}`)
    } catch (error) {
      setErrorMessage(error.message)
    }
  }

  const openAuthModal = (mode) => {
    setAuthMode(mode)
    setAuthModalOpen(true)
    setAuthError('')
  }

  const gameStatus =
    game?.result === true
      ? 'Won'
      : game?.result === false
        ? 'Lost'
        : game
          ? 'In progress'
          : 'Idle'

  return (
    <div className="app">
      <header className="hero">
        <div className="hero-top">
          <span className="pill">{token ? `Signed in as ${userName}` : 'Signed out'}</span>
          <div className="auth-actions">
            {token ? (
              <button className="ghost white" onClick={handleLogout}>
                Log out
              </button>
            ) : (
              <>
                <button
                  className="ghost white"
                  onClick={() => openAuthModal('login')}
                >
                  Login
                </button>
              </>
            )}
          </div>
        </div>

        <div className="hero-body">
          <div className="hero-title">
            <h1>Wordle</h1>
            <p className="hero-subtitle">
              Guess the word in six tries. Each guess shows which letters match.
            </p>
          </div>
        </div>
      </header>

      <main className="grid">
        {!game ? (
          <section className="panel setup-panel">
            <div className="panel-header">
              <h2>Game Setup</h2>
              <span className="pill">{gameStatus}</span>
            </div>

            <div className="difficulty">
              {DIFFICULTIES.map((item) => (
                <button
                  key={item.id}
                  className={difficulty === item.id ? 'active' : ''}
                  onClick={() => setDifficulty(item.id)}
                >
                  <span>{item.label}</span>
                  <small>{item.wordLength} letters</small>
                </button>
              ))}
            </div>

            <div className="stack">
              <button onClick={startNewGame} disabled={isBusy}>
                Start Wordle
              </button>
              <p className="subtle">
                Pick a difficulty and press start to load a new word.
              </p>
            </div>
          </section>
        ) : null}

        {game ? (
          <section className="panel board-panel">
            <div className="panel-header">
              <div>
                <h2>Wordle Grid</h2>
                <p className="subtle">
                  Difficulty:{' '}
                  {DIFFICULTIES.find((item) => item.id === difficulty)?.label ||
                    'Custom'}
                </p>
              </div>
              <button className="ghost white" onClick={quitGame}>
                Quit game
              </button>
            </div>

            <div className="board" style={{ '--word-length': wordLength }}>
              {boardRows.map((row, rowIndex) => (
                <div className="board-row" key={`row-${rowIndex}`}>
                  {Array.from({ length: wordLength }, (_, colIndex) => {
                    const letter =
                      row.type === 'guess'
                        ? row.letters[colIndex]?.char?.toUpperCase() || ''
                        : row.type === 'active'
                          ? row.letters[colIndex] || ''
                          : ''
                    const status =
                      row.type === 'guess'
                        ? row.letters[colIndex]?.status
                        : null
                    const isActive = row.type === 'active'
                    const isInvalid = isActive && invalidWordActive
                    return (
                      <div
                        className={`tile ${status || ''} ${
                          isActive && letter ? 'filled' : ''
                        } ${isInvalid ? 'invalid' : ''}`}
                        key={`tile-${rowIndex}-${colIndex}`}
                      >
                        {letter}
                      </div>
                    )
                  })}
                </div>
              ))}
            </div>

            <div className="guess-input">
              <input
                value={currentGuess}
                onChange={(event) =>
                  setCurrentGuess(
                    event.target.value
                      .replace(/[^a-zA-Z]/g, '')
                      .toUpperCase()
                      .slice(0, wordLength)
                  )
                }
                placeholder={`Enter ${wordLength}-letter guess`}
                disabled={!game || game.result !== null}
              />
              <button
                onClick={submitGuess}
                disabled={
                  !game || isBusy || currentGuess.length !== wordLength
                }
              >
                Send
              </button>
            </div>

            {invalidWordMessage ? (
              <p className="word-error">{invalidWordMessage}</p>
            ) : null}

            <div className="keyboard">
              {KEYBOARD_ROWS.map((row) => (
                <div className="keyboard-row" key={row}>
                  {row.split('').map((letter) => (
                    <button
                      key={letter}
                      className={`key ${letterStatus[letter] || ''}`}
                      onClick={() => addLetter(letter)}
                      disabled={!game || game.result !== null}
                    >
                      {letter}
                    </button>
                  ))}
                  {row === 'ZXCVBNM' ? (
                    <>
                      <button
                        className="key wide"
                        onClick={removeLetter}
                        disabled={!game || game.result !== null}
                      >
                        Back
                      </button>
                      <button
                        className="key wide primary"
                        onClick={submitGuess}
                        disabled={
                          !game ||
                          game.result !== null ||
                          currentGuess.length !== wordLength ||
                          isBusy
                        }
                      >
                        Enter
                      </button>
                    </>
                  ) : null}
                </div>
              ))}
            </div>
          </section>
        ) : null}
      </main>

      <section className="panel feedback">
        <div>
          <p className="label">System</p>
          {statusMessage ? <p className="status">{statusMessage}</p> : null}
          {errorMessage ? <p className="error">{errorMessage}</p> : null}
        </div>
        <div className="backend-tools">
          <div className="backend-tools-header">
            <p className="label">Backend testing</p>
            <button
              type="button"
              className="ghost white"
              onClick={() => setShowBackendTools((prev) => !prev)}
            >
              {showBackendTools ? 'Hide' : 'Show'}
            </button>
          </div>
          {showBackendTools ? (
            <div className="backend-tools-body">
              <input
                value={apiBase}
                onChange={(event) => setApiBase(event.target.value)}
                placeholder="http://localhost:8080"
              />
              <div className="hero-status">
                <span className="pill">
                  {token ? 'Authenticated' : 'Signed out'}
                </span>
                <div className="backend-actions">
                  <button
                    type="button"
                    className="h2-button"
                    onClick={() =>
                      window.open(
                        `${apiBase.replace(/\/+$/, '')}/h2-console`,
                        '_blank',
                        'noreferrer'
                      )                        
                    }
                  >
                    Open H2 Console
                  </button>
                  <button onClick={handlePing} disabled={isBusy}>
                    Ping server
                  </button>
                </div>
              </div>
            </div>
          ) : null}
        </div>
      </section>

      {authModalOpen ? (
        <div className="modal-backdrop">
          <div className="modal">
            <div className="modal-header">
              <h3>{authMode === 'login' ? 'Sign in' : 'Create account'}</h3>
              <button
                type="button"
                className="close-button"
                onClick={() => setAuthModalOpen(false)}
                aria-label="Close"
              >
                ×
              </button>
            </div>
            <div className="tabs modal-tabs">
              <button
                className={authMode === 'login' ? 'active' : ''}
                onClick={() => {
                  setAuthMode('login')
                  setAuthError('')
                }}
                type="button"
              >
                Sign in
              </button>
              <button
                className={authMode === 'register' ? 'active' : ''}
                onClick={() => {
                  setAuthMode('register')
                  setAuthError('')
                }}
                type="button"
              >
                Register
              </button>
            </div>
            <form
              className="stack"
              onSubmit={authMode === 'login' ? handleLogin : handleRegister}
            >
              <label>
                Username
                <input
                  className={authError ? 'auth-input invalid' : 'auth-input'}
                  value={userName}
                  onChange={(event) => setUserName(event.target.value)}
                  autoComplete="username"
                  required
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  className={authError ? 'auth-input invalid' : 'auth-input'}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  autoComplete={
                    authMode === 'login' ? 'current-password' : 'new-password'
                  }
                  required
                />
              </label>
              <button type="submit" disabled={isBusy}>
                {authMode === 'login' ? 'Sign in' : 'Create account'}
              </button>
              {authError ? (
                <p className="auth-error">{authError}</p>
              ) : null}
            </form>
          </div>
        </div>
      ) : null}

      {modal.open ? (
        <div className="modal-backdrop">
          <div className="modal">
            <h3>{modal.title}</h3>
            <p>{modal.message}</p>
            <button onClick={() => setModal({ open: false, title: '', message: '' })}>
              Close
            </button>
          </div>
        </div>
      ) : null}
    </div>
  )
}

export default App
