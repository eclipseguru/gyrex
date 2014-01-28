/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields;

import java.io.InputStream;

public interface IUploadAdapter {

	/**
	 * Called by the {@link UploadDialogField} to receive the uploaded data.
	 * <p>
	 * Maybe called outside UI thread.
	 * </p>
	 * 
	 * @param stream
	 * @param fileName
	 * @param contentType
	 * @param contentLength
	 */
	void receive(InputStream stream, String fileName, String contentType, long contentLength);

}
