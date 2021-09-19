/* --------------------------------------------------------------------------------------------
 * Based on https://github.com/microsoft/vscode-extension-samples, Copyright (c) Microsoft Corporation under MIT License
 *
 * Adapted for Codyze. License: Apache 2.0
 * ------------------------------------------------------------------------------------------ */

import * as path from 'path';
import { ExtensionContext, workspace } from 'vscode';

import {
	LanguageClient,
	LanguageClientOptions,
	ServerOptions
} from 'vscode-languageclient/node';

let client: LanguageClient;

export function activate(context: ExtensionContext) {
	let markPath = context.asAbsolutePath(path.join("codyze", "mark"))

	let serverModule = context.asAbsolutePath(
		path.join('codyze', 'bin', 'codyze')
	);

	// If the extension is launched in debug mode then the debug server options are used
	// Otherwise the run options are used
	let serverOptions: ServerOptions = {
		run: { command: serverModule, args: ["-l", "-m", markPath] },
		debug: { command: serverModule, args: ["-l", "-m", markPath] }
	};

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'cpp' }, { scheme: 'file', language: 'java' }],
		synchronize: {
			// Notify the server about file changes to '.clientrc files contained in the workspace
			fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
		}
	};

	// Create the language client and start the client.
	client = new LanguageClient(
		'vscode-codyze',
		'Codyze',
		serverOptions,
		clientOptions
	);

	// Start the client. This will also launch the server
	client.start();
}

export function deactivate(): Thenable<void> | undefined {
	if (!client) {
		return undefined;
	}

	return client.stop();
}
