import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class Main {
	
	static final Semaphore tables = new Semaphore(3);
	static final Semaphore waiter = new Semaphore(1);
	
	private static boolean isAvailableHours = true;
	private static final List<Thread> trackThreadList = new CopyOnWriteArrayList<>();

	public static synchronized boolean isOpen () {
		return isAvailableHours;
	}
	
	public static synchronized void closeCafe () {
		isAvailableHours = false;
		 System.err.println("=============Кафе закрили================");
	}

	public static void main(String[] args) throws InterruptedException {
		
		Runnable cafe = () -> {
			int i = 0;
			while(isOpen()) {
				Thread trackThread = new Thread(new People(), String.valueOf(i));
				trackThreadList.add(trackThread);
				trackThread.start();
				i++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		};
		
		Thread cafeThread = new Thread(cafe, "Кафе");
		cafeThread.start();
		Thread.sleep(6000);

		closeCafe();

		for (Thread trackThread : trackThreadList) {
			trackThread.join();
		}


		
		System.err.println("=============Персонал пішов додому================");

	}

}
