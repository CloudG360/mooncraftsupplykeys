package io.cg360.moon.supplykeys.tasks.drop;

import io.cg360.moon.supplykeys.Supplykeys;
import io.cg360.moon.supplykeys.entities.SupplyCrate;
import org.spongepowered.api.scheduler.Task;

import java.util.Optional;
import java.util.function.Consumer;

public class RunnableCheckSupplyDrop implements Consumer<Task> {

    private String supplyDropID;

    public RunnableCheckSupplyDrop(String supplyDropID) {
        this.supplyDropID = supplyDropID;
    }

    @Override
    public void accept(Task task) {
        Optional<SupplyCrate> active = Supplykeys.getSKPlugin().getSupplyDropManager().getActiveCrate(supplyDropID);
        if(!active.isPresent()){
            task.cancel();
            return;
        }
        SupplyCrate c = active.get();
        try {
            boolean result = c.process();
            if(result){
                task.cancel();
            }
        } catch(Exception err){
            err.printStackTrace();
            task.cancel();
        }

    }

}
