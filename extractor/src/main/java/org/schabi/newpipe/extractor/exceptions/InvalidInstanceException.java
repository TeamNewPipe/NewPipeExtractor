package org.schabi.newpipe.extractor.exceptions;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvalidInstanceException.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvalidInstanceException extends ExtractionException {
    public InvalidInstanceException(String message) {
        super(message);
    }

    public InvalidInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
