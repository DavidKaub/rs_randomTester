package jzombies;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.ContextUtils;

public class JZombiesBuilder implements ContextBuilder<Object> {
	
	
	private Grid<Object> grid;

	/*
	 * This method creates some random numbers and terminates the simulation after
	 * 10 ticks. Then the sum of all coordinates of all Zombies as well as a
	 * multiplication of some random numbers are exited
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void step() {
		double test = 0;
		for (int i = 0; i < RandomHelper.nextIntFromTo(1, 10); i++) {
			test = RandomHelper.nextDouble();
		}

		if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() > 10) {
			Iterator it = ContextUtils.getContext(this).getObjects(Zombie.class).iterator();
			int x = 0;
			int y = 0;
			while (it.hasNext()) {
				Zombie z = (Zombie) it.next();

				x += grid.getLocation(z).getX();
				y += grid.getLocation(z).getY();
			}

			System.out.println(
					"x = " + x + " y = " + y + " test = " + test * RandomHelper.nextDoubleFromTo(1.0d, 100.0d));
			RunEnvironment.getInstance().endRun();
		}
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context
	 * .Context)
	 */
	@Override
	public Context build(Context<Object> context) {
		context.setId("jzombies");

		
		//here I set the random seed to a static value to ensure constant input
		RandomHelper.setSeed(1);

		//here I add the JZombiesBuilder to the context to enable the call of the ScheduledMethod
		context.add(this);
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"infection network", context, true);
		netBuilder.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		Parameters params = RunEnvironment.getInstance().getParameters();
		int zombieCount = (Integer) params.getValue("zombie_count");
		for (int i = 0; i < zombieCount; i++) {
			context.add(new Zombie(space, grid));
		}

		int humanCount = (Integer) params.getValue("human_count");
		for (int i = 0; i < humanCount; i++) {
			int energy = RandomHelper.nextIntFromTo(4, 10);
			context.add(new Human(space, grid, energy));
		}

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20);
		}

		return context;
	}
}
