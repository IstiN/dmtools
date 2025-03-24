tell application "System Events"
    -- Get frontmost application process
    set frontApp to first application process whose frontmost is true

    log "{"
    log "  \"application\": \"" & name of frontApp & "\","

    tell frontApp
        -- Get the frontmost window
        set frontWindow to window 1

        log "  \"window\": {"
        log "    \"title\": \"" & name of frontWindow & "\","
        log "    \"position\": " & (position of frontWindow) & ","
        log "    \"size\": " & (size of frontWindow) & ","

        -- Get all accessible elements
        log "    \"elements\": ["

        set allElements to every UI element of frontWindow
        repeat with i from 1 to count of allElements
            set currentElement to item i of allElements

            try
                log "      {"
                log "        \"role\": \"" & role of currentElement & "\","
                log "        \"description\": \"" & description of currentElement & "\","
                log "        \"position\": " & (position of currentElement) & ","
                log "        \"size\": " & (size of currentElement)

                if i < count of allElements then
                    log "      },"
                else
                    log "      }"
                end if
            end try
        end repeat

        log "    ]"
        log "  }"
    end tell
    log "}"
end tell