package net.coderbot.iris.gl.uniform;

public abstract class Uniform<T> {
	protected final String name;
	protected final UniformType type;
	protected final int location;
	protected final ValueUpdateNotifier notifier;

	Uniform(String name, UniformType type, int location) {
		this(name, type, location, null);
	}

	Uniform(String name, UniformType type, int location, ValueUpdateNotifier notifier) {
		this.name = name;
		this.type = type;
		this.location = location;
		this.notifier = notifier;
	}

	public abstract T getValue();

	public abstract void update();

	public final int getLocation() {
		return location;
	}

	public abstract int getByteSize();

	public final ValueUpdateNotifier getNotifier() {
		return notifier;
	}

	public String getName() {
		return name;
	}

	public UniformType getType() {
		return type;
	}
}
