package per.nonlone.expandtweak.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import per.nonlone.expandtweak.common.Observable;
/**
 * 
 * @author Nonlone
 * @email thunderbird.shun@gmail.com
 *
 */
public class ObservedThreadPool extends ThreadPoolExecutor implements Observable<Enum<ObservedThreadPool.State>> {

	private static final Logger log = Logger.getLogger(ObservedThreadPool.class);

	/**
	 * 线程池状态
	 * 
	 * @author Nonlone
	 * @email thunderbird.shun@gmail.com
	 *
	 */
	public enum State {
		FREE, WAIT, BUSY, FULL
	}

	private State state = State.FREE;

	public ObservedThreadPool(int corePoolSize, int maximumPoolSize, int queueSize) {
		super(corePoolSize, maximumPoolSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueSize));
	}

	public void notifiedChange(Enum<State> t) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		State checkState = checkState();
		boolean setResult = false;
		if (checkState.ordinal() < state.ordinal()) {
			setResult = setState(checkState, state);
		}
		log.info(String.format("op<beforeExecute> setResult<%b>", setResult));
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		State checkState = checkState();
		boolean setResult = false;
		// 当前等级大于状态等级才升级
		if (checkState.ordinal() > state.ordinal()) {
			setResult = setState(checkState, state);
		}
		log.info(String.format("op<beforeExecute> setResult<%b>", setResult));
	}

	/**
	 * 检查当前线程池状态
	 * 
	 * @return state 状态枚举类
	 */
	private State checkState() {
		State resultState = null;
		if (getActiveCount() <= getCorePoolSize()) {
			resultState = State.FREE;
		} else if (getActiveCount() > getCorePoolSize() && getQueue().remainingCapacity() > 0) {
			resultState = State.WAIT;
		} else if (getQueue().remainingCapacity() == 0) {
			resultState = State.BUSY;
		} else if (getActiveCount() > getCorePoolSize() && getActiveCount() < getMaximumPoolSize()) {
			resultState = State.FULL;
		}
		return Optional.of(resultState).or(State.FULL);
	}

	private boolean setState(State newState, State srcState) {
		boolean result = false;
		ReentrantLock lock = new ReentrantLock();
		try {
			// 状态相同，直接放弃修改
			if (state != srcState) {
				return false;
			} else {
				lock.lock();
				if (state != srcState) {
					return false;
				} else {
					state = newState;
					result = true;
				}
			}
		} catch (Exception e) {
			log.error(String.format("op<setState> newState<%s> srcState<%s>", newState, srcState), e);
			result = false;
		} finally {
			lock.unlock();
		}
		// 通知接口
		if (result) {
			notifiedChange(state);
		}
		return result;
	}
}
