#pragma once

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "AND64INLINEHOOK.hpp"

inline uintptr_t findLibrary(const char *library)
{
    FILE *fp = fopen("/proc/self/maps", "rt");

    if (fp == nullptr)
        return 0;

    char line[1024];
    uintptr_t address = 0;

    while (fgets(line, sizeof(line), fp))
    {
        if (strstr(line, library))
        {
            address = static_cast<uintptr_t>(strtoull(line, nullptr, 16));
            break;
        }
    }

    fclose(fp);

    return address;
}

inline uintptr_t getAbsoluteAddress(const char *library, uintptr_t offset)
{
    uintptr_t base = findLibrary(library);

    if (base == 0)
        return 0;

    return base + offset;
}

inline bool isLibraryLoaded(const char *library)
{
    return findLibrary(library) != 0;
}

inline void hookFunction(void *target, void *replacement, void **original)
{
    A64HookFunction(target, replacement, original);
}