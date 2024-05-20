@echo off
setlocal EnableDelayedExpansion

set "prevFile="

for /F "tokens=1,2 delims=," %%a in (.env) do (
    echo Processing line: %%a,%%b
    for /F "tokens=1,2 delims==" %%c in ("%%b") do (
        echo File: %%a
        echo Variable: %%c
        echo Value: %%d

        if not "%%a"=="!prevFile!" (
            if exist %%a (
                del %%a
                echo Deleted file: %%a
            )
            type nul > %%a
            echo Created file: %%a
        )

        echo. >> %%a
        echo %%c=%%d >> %%a
        echo Added variable to file

        set "prevFile=%%a"
    )
)
