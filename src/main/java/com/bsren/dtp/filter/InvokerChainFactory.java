package com.bsren.dtp.filter;

public final class InvokerChainFactory {

    private InvokerChainFactory() { }

    @SafeVarargs
    public static<T> InvokerChain<T> buildInvokerChain(Invoker<T> target, Filter<T>... filters) {

        InvokerChain<T> invokerChain = new InvokerChain<>();
        Invoker<T> last = target;
        for (int i = filters.length - 1; i >= 0; i--) {
            Invoker<T> next = last;
            Filter<T> filter = filters[i];
            last = new Invoker<T>() {
                @Override
                public void invoke(T context) {
                    filter.doFilter(context, next);
                }
            };
        }
        invokerChain.setHead(last);
        return invokerChain;
    }
}


