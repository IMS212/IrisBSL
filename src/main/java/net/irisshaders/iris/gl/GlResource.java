package net.irisshaders.iris.gl;

public abstract class GlResource {
	private int id;
	private boolean isValid;

	protected GlResource(int id) {
		this.id = id;
		isValid = true;
	}

	public final void destroy() {
		destroyInternal();
		isValid = false;
	}

	public final void changeId(int id) {
		this.id = id;
		isValid = true;
	}

	protected abstract void destroyInternal();

	protected void assertValid() {
		if (!isValid) {
			throw new IllegalStateException("Tried to use a destroyed GlResource");
		}
	}

	protected int getGlId() {
		assertValid();

		return id;
	}
}
