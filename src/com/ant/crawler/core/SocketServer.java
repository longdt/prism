package com.ant.crawler.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketServer {
	private MainExecutor executor;
	private ExecutorService pool;
	private ServerSocket server;
	private List<Worker> workers;
	private boolean running;
	private Runtime runtime = Runtime.getRuntime();
	public SocketServer(MainExecutor executor, int port) throws IOException {
		this.executor = executor;
		this.workers = executor.getWorkers();
		this.pool = Executors.newCachedThreadPool();
		server = new ServerSocket(port);
		running = true;
	}

	public void execute() {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					while (running && !Thread.currentThread().isInterrupted() && !pool.isTerminated()) {
						Socket socket = server.accept();
						pool.execute(new ServerHandler(socket));
					}
				} catch (Exception e) {

				}
			}
		});
	}
	
	class ServerHandler implements Runnable {
		private Socket socket;

		public ServerHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				String command = null;
				writer.println("Hello Guest!");
				while (!Thread.currentThread().isInterrupted() && (command = reader.readLine()) != null) {
					if (command.equals("shutdown")) {
						writer.println("+OK");
						(new Thread() {
							public void run() {
								try {
									server.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								for (Worker worker : workers) {
									worker.shutdown();
								}
								executor.shutdown();
								pool.shutdownNow();
							};
						}).start();
						break;
					} else if (command.equals("quit")) {
						writer.println("Bye");
						break;
					} else if (command.equals("status")) {
						writer.println("Total Mem: " + runtime.totalMemory() / (1024 * 1024) + "Mbs");
						writer.println("+OK");
					} else if (command.equals("sync")) {
						writer.println("synchronizing...");
						writer.println("+OK");
					}
				}
			} catch (Exception e) {
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


	}

	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			
		}
	}
}

