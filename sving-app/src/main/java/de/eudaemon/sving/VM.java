package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.Objects;

public class VM {
    final VirtualMachineDescriptor descriptor;

    VM(VirtualMachineDescriptor descriptor_) {
        descriptor = descriptor_;
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    @Override
    public boolean equals(Object o_) {
        if (this == o_) return true;
        if (o_ == null || getClass() != o_.getClass()) return false;
        VM vm = (VM) o_;
        return Objects.equals(descriptor, vm.descriptor);
    }

    @Override
    public String toString() {
        return descriptor.displayName();
    }
}
